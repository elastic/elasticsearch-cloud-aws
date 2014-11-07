/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.cloud.aws.blobstore;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.BlobStoreException;
import org.elasticsearch.common.blobstore.support.AbstractBlobContainer;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class S3BlobContainer extends AbstractBlobContainer {

    protected final S3BlobStore blobStore;

    protected final String keyPath;

    public S3BlobContainer(BlobPath path, S3BlobStore blobStore) {
        super(path);
        this.blobStore = blobStore;
        String keyPath = path.buildAsString("/");
        if (!keyPath.isEmpty()) {
            keyPath = keyPath + "/";
        }
        this.keyPath = keyPath;
    }

    @Override
    public boolean blobExists(String blobName) {
        try {
            blobStore.client().getObjectMetadata(blobStore.bucket(), buildKey(blobName));
            return true;
        } catch (AmazonS3Exception e) {
            return false;
        } catch (Throwable e) {
            throw new BlobStoreException("failed to check if blob exists", e);
        }
    }

    @Override
    public void deleteBlob(String blobName) throws IOException {
        try {
            blobStore.client().deleteObject(blobStore.bucket(), buildKey(blobName));
        } catch (AmazonClientException e) {
            throw new IOException("Exception when deleting blob [" + blobName + "]", e);
        }
    }

    @Override
    public InputStream openInput(String blobName) throws IOException {
        int retry = 0;
        while (retry <= blobStore.numberOfRetries()) {
            try {
                S3Object s3Object = blobStore.client().getObject(blobStore.bucket(), buildKey(blobName));
                return s3Object.getObjectContent();
            } catch (AmazonClientException e) {
                if (blobStore.shouldRetry(e) && (retry < blobStore.numberOfRetries())) {
                    retry++;
                } else {
                    if (e instanceof AmazonS3Exception) {
                        if (404 == ((AmazonS3Exception) e).getStatusCode()) {
                            throw new FileNotFoundException(e.getMessage());
                        }
                    }
                    throw e;
                }
            }
        }
        throw new BlobStoreException("retries exhausted while attempting to access blob object [name:" + blobName + ", bucket:" + blobStore.bucket() +"]");
    }

    @Override
    public OutputStream createOutput(final String blobName) throws IOException {
        // UploadS3OutputStream does buffering & retry logic internally
        return new DefaultS3OutputStream(blobStore, blobStore.bucket(), buildKey(blobName), blobStore.bufferSizeInBytes(), blobStore.numberOfRetries(), blobStore.serverSideEncryption());
    }

    @Override
    public ImmutableMap<String, BlobMetaData> listBlobsByPrefix(@Nullable String blobNamePrefix) throws IOException {
        ImmutableMap.Builder<String, BlobMetaData> blobsBuilder = ImmutableMap.builder();
        ObjectListing prevListing = null;
        while (true) {
            ObjectListing list;
            if (prevListing != null) {
                list = blobStore.client().listNextBatchOfObjects(prevListing);
            } else {
                if (blobNamePrefix != null) {
                    list = blobStore.client().listObjects(blobStore.bucket(), buildKey(blobNamePrefix));
                } else {
                    list = blobStore.client().listObjects(blobStore.bucket(), keyPath);
                }
            }
            for (S3ObjectSummary summary : list.getObjectSummaries()) {
                String name = summary.getKey().substring(keyPath.length());
                blobsBuilder.put(name, new PlainBlobMetaData(name, summary.getSize()));
            }
            if (list.isTruncated()) {
                prevListing = list;
            } else {
                break;
            }
        }
        return blobsBuilder.build();
    }

    @Override
    public ImmutableMap<String, BlobMetaData> listBlobs() throws IOException {
        return listBlobsByPrefix(null);
    }

    protected String buildKey(String blobName) {
        return keyPath + blobName;
    }

}
