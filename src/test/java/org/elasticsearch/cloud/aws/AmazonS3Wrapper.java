/*
 * Licensed to Elasticsearch (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.elasticsearch.cloud.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.S3ResponseMetadata;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration;
import com.amazonaws.services.s3.model.intelligenttiering.IntelligentTieringConfiguration;
import com.amazonaws.services.s3.model.inventory.InventoryConfiguration;
import com.amazonaws.services.s3.model.metrics.MetricsConfiguration;
import com.amazonaws.services.s3.model.ownership.OwnershipControls;
import com.amazonaws.services.s3.waiters.AmazonS3Waiters;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class AmazonS3Wrapper implements AmazonS3 {

    protected AmazonS3 delegate;

    public AmazonS3Wrapper(AmazonS3 delegate) {
        this.delegate = delegate;
    }


    @Override
    public void setEndpoint(String endpoint) {
        delegate.setEndpoint(endpoint);
    }

    @Override
    public void setRegion(Region region) throws IllegalArgumentException {
        delegate.setRegion(region);
    }

    @Override
    public void setS3ClientOptions(S3ClientOptions clientOptions) {
        delegate.setS3ClientOptions(clientOptions);
    }

    @Override
    public void changeObjectStorageClass(String bucketName, String key, StorageClass newStorageClass) throws AmazonClientException, AmazonServiceException {
        delegate.changeObjectStorageClass(bucketName, key, newStorageClass);
    }

    @Override
    public void setObjectRedirectLocation(String bucketName, String key, String newRedirectLocation) throws AmazonClientException, AmazonServiceException {
        delegate.setObjectRedirectLocation(bucketName, key, newRedirectLocation);
    }

    @Override
    public ObjectListing listObjects(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.listObjects(bucketName);
    }

    @Override
    public ObjectListing listObjects(String bucketName, String prefix) throws AmazonClientException, AmazonServiceException {
        return delegate.listObjects(bucketName, prefix);
    }

    @Override
    public ObjectListing listObjects(ListObjectsRequest listObjectsRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.listObjects(listObjectsRequest);
    }

    @Override
    public ObjectListing listNextBatchOfObjects(ObjectListing previousObjectListing) throws AmazonClientException, AmazonServiceException {
        return delegate.listNextBatchOfObjects(previousObjectListing);
    }

    @Override
    public VersionListing listVersions(String bucketName, String prefix) throws AmazonClientException, AmazonServiceException {
        return delegate.listVersions(bucketName, prefix);
    }

    @Override
    public VersionListing listNextBatchOfVersions(VersionListing previousVersionListing) throws AmazonClientException, AmazonServiceException {
        return delegate.listNextBatchOfVersions(previousVersionListing);
    }

    @Override
    public VersionListing listVersions(String bucketName, String prefix, String keyMarker, String versionIdMarker, String delimiter, Integer maxResults) throws AmazonClientException, AmazonServiceException {
        return delegate.listVersions(bucketName, prefix, keyMarker, versionIdMarker, delimiter, maxResults);
    }

    @Override
    public VersionListing listVersions(ListVersionsRequest listVersionsRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.listVersions(listVersionsRequest);
    }

    @Override
    public Owner getS3AccountOwner() throws AmazonClientException, AmazonServiceException {
        return delegate.getS3AccountOwner();
    }

    @Override
    public boolean doesBucketExist(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.doesBucketExist(bucketName);
    }

    @Override
    public HeadBucketResult headBucket(HeadBucketRequest headBucketRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.headBucket(headBucketRequest);
    }

    @Override
    public List<Bucket> listBuckets() throws AmazonClientException, AmazonServiceException {
        return delegate.listBuckets();
    }

    @Override
    public List<Bucket> listBuckets(ListBucketsRequest listBucketsRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.listBuckets(listBucketsRequest);
    }

    @Override
    public String getBucketLocation(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketLocation(bucketName);
    }

    @Override
    public String getBucketLocation(GetBucketLocationRequest getBucketLocationRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketLocation(getBucketLocationRequest);
    }

    @Override
    public Bucket createBucket(CreateBucketRequest createBucketRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.createBucket(createBucketRequest);
    }

    @Override
    public Bucket createBucket(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.createBucket(bucketName);
    }

    @Override
    public Bucket createBucket(String bucketName, com.amazonaws.services.s3.model.Region region) throws AmazonClientException, AmazonServiceException {
        return delegate.createBucket(bucketName, region);
    }

    @Override
    public Bucket createBucket(String bucketName, String region) throws AmazonClientException, AmazonServiceException {
        return delegate.createBucket(bucketName, region);
    }

    @Override
    public AccessControlList getObjectAcl(String bucketName, String key) throws AmazonClientException, AmazonServiceException {
        return delegate.getObjectAcl(bucketName, key);
    }

    @Override
    public AccessControlList getObjectAcl(String bucketName, String key, String versionId) throws AmazonClientException, AmazonServiceException {
        return delegate.getObjectAcl(bucketName, key, versionId);
    }

    @Override
    public AccessControlList getObjectAcl(GetObjectAclRequest getObjectAclRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.getObjectAcl(getObjectAclRequest);
    }

    @Override
    public void setObjectAcl(String bucketName, String key, AccessControlList acl) throws AmazonClientException, AmazonServiceException {
        delegate.setObjectAcl(bucketName, key, acl);
    }

    @Override
    public void setObjectAcl(String bucketName, String key, CannedAccessControlList acl) throws AmazonClientException, AmazonServiceException {
        delegate.setObjectAcl(bucketName, key, acl);
    }

    @Override
    public void setObjectAcl(String bucketName, String key, String versionId, AccessControlList acl) throws AmazonClientException, AmazonServiceException {
        delegate.setObjectAcl(bucketName, key, versionId, acl);
    }

    @Override
    public void setObjectAcl(String bucketName, String key, String versionId, CannedAccessControlList acl) throws AmazonClientException, AmazonServiceException {
        delegate.setObjectAcl(bucketName, key, versionId, acl);
    }

    @Override
    public void setObjectAcl(SetObjectAclRequest setObjectAclRequest) throws AmazonClientException, AmazonServiceException {
        delegate.setObjectAcl(setObjectAclRequest);
    }

    @Override
    public AccessControlList getBucketAcl(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketAcl(bucketName);
    }

    @Override
    public void setBucketAcl(SetBucketAclRequest setBucketAclRequest) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketAcl(setBucketAclRequest);
    }

    @Override
    public AccessControlList getBucketAcl(GetBucketAclRequest getBucketAclRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketAcl(getBucketAclRequest);
    }

    @Override
    public void setBucketAcl(String bucketName, AccessControlList acl) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketAcl(bucketName, acl);
    }

    @Override
    public void setBucketAcl(String bucketName, CannedAccessControlList acl) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketAcl(bucketName, acl);
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String key) throws AmazonClientException, AmazonServiceException {
        return delegate.getObjectMetadata(bucketName, key);
    }

    @Override
    public ObjectMetadata getObjectMetadata(GetObjectMetadataRequest getObjectMetadataRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.getObjectMetadata(getObjectMetadataRequest);
    }

    @Override
    public S3Object getObject(String bucketName, String key) throws AmazonClientException, AmazonServiceException {
        return delegate.getObject(bucketName, key);
    }

    @Override
    public S3Object getObject(GetObjectRequest getObjectRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.getObject(getObjectRequest);
    }

    @Override
    public ObjectMetadata getObject(GetObjectRequest getObjectRequest, File destinationFile) throws AmazonClientException, AmazonServiceException {
        return delegate.getObject(getObjectRequest, destinationFile);
    }

    @Override
    public void deleteBucket(DeleteBucketRequest deleteBucketRequest) throws AmazonClientException, AmazonServiceException {
        delegate.deleteBucket(deleteBucketRequest);
    }

    @Override
    public void deleteBucket(String bucketName) throws AmazonClientException, AmazonServiceException {
        delegate.deleteBucket(bucketName);
    }

    @Override
    public void setBucketReplicationConfiguration(String bucketName, BucketReplicationConfiguration configuration) throws AmazonServiceException, AmazonClientException {
        delegate.setBucketReplicationConfiguration(bucketName, configuration);
    }

    @Override
    public void setBucketReplicationConfiguration(SetBucketReplicationConfigurationRequest setBucketReplicationConfigurationRequest) throws AmazonServiceException, AmazonClientException {
        delegate.setBucketReplicationConfiguration(setBucketReplicationConfigurationRequest);
    }

    @Override
    public BucketReplicationConfiguration getBucketReplicationConfiguration(String bucketName) throws AmazonServiceException, AmazonClientException {
        return delegate.getBucketReplicationConfiguration(bucketName);
    }

    @Override
    public void deleteBucketReplicationConfiguration(String bucketName) throws AmazonServiceException, AmazonClientException {
        delegate.deleteBucketReplicationConfiguration(bucketName);
    }

    @Override
    public void deleteBucketReplicationConfiguration(DeleteBucketReplicationConfigurationRequest request) throws AmazonServiceException,
            AmazonClientException {
        delegate.deleteBucketReplicationConfiguration(request);
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectName) throws AmazonServiceException, AmazonClientException {
        return delegate.doesObjectExist(bucketName, objectName);
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest putObjectRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.putObject(putObjectRequest);
    }

    @Override
    public PutObjectResult putObject(String bucketName, String key, File file) throws AmazonClientException, AmazonServiceException {
        return delegate.putObject(bucketName, key, file);
    }

    @Override
    public PutObjectResult putObject(String bucketName, String key, InputStream input, ObjectMetadata metadata) throws AmazonClientException, AmazonServiceException {
        return delegate.putObject(bucketName, key, input, metadata);
    }

    @Override
    public CopyObjectResult copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) throws AmazonClientException, AmazonServiceException {
        return delegate.copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
    }

    @Override
    public CopyObjectResult copyObject(CopyObjectRequest copyObjectRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.copyObject(copyObjectRequest);
    }

    @Override
    public CopyPartResult copyPart(CopyPartRequest copyPartRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.copyPart(copyPartRequest);
    }

    @Override
    public void deleteObject(String bucketName, String key) throws AmazonClientException, AmazonServiceException {
        delegate.deleteObject(bucketName, key);
    }

    @Override
    public void deleteObject(DeleteObjectRequest deleteObjectRequest) throws AmazonClientException, AmazonServiceException {
        delegate.deleteObject(deleteObjectRequest);
    }

    @Override
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest deleteObjectsRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.deleteObjects(deleteObjectsRequest);
    }

    @Override
    public void deleteVersion(String bucketName, String key, String versionId) throws AmazonClientException, AmazonServiceException {
        delegate.deleteVersion(bucketName, key, versionId);
    }

    @Override
    public void deleteVersion(DeleteVersionRequest deleteVersionRequest) throws AmazonClientException, AmazonServiceException {
        delegate.deleteVersion(deleteVersionRequest);
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketLoggingConfiguration(bucketName);
    }

    @Override
    public void setBucketLoggingConfiguration(SetBucketLoggingConfigurationRequest setBucketLoggingConfigurationRequest) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketLoggingConfiguration(setBucketLoggingConfigurationRequest);
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketVersioningConfiguration(bucketName);
    }

    @Override
    public void setBucketVersioningConfiguration(SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest);
    }

    @Override
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(String bucketName) {
        return delegate.getBucketLifecycleConfiguration(bucketName);
    }

    @Override
    public void setBucketLifecycleConfiguration(String bucketName, BucketLifecycleConfiguration bucketLifecycleConfiguration) {
        delegate.setBucketLifecycleConfiguration(bucketName, bucketLifecycleConfiguration);
    }

    @Override
    public void setBucketLifecycleConfiguration(SetBucketLifecycleConfigurationRequest setBucketLifecycleConfigurationRequest) {
        delegate.setBucketLifecycleConfiguration(setBucketLifecycleConfigurationRequest);
    }

    @Override
    public void deleteBucketLifecycleConfiguration(String bucketName) {
        delegate.deleteBucketLifecycleConfiguration(bucketName);
    }

    @Override
    public void deleteBucketLifecycleConfiguration(DeleteBucketLifecycleConfigurationRequest deleteBucketLifecycleConfigurationRequest) {
        delegate.deleteBucketLifecycleConfiguration(deleteBucketLifecycleConfigurationRequest);
    }

    @Override
    public BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(String bucketName) {
        return delegate.getBucketCrossOriginConfiguration(bucketName);
    }

    @Override
    public void setBucketCrossOriginConfiguration(String bucketName, BucketCrossOriginConfiguration bucketCrossOriginConfiguration) {
        delegate.setBucketCrossOriginConfiguration(bucketName, bucketCrossOriginConfiguration);
    }

    @Override
    public void setBucketCrossOriginConfiguration(SetBucketCrossOriginConfigurationRequest setBucketCrossOriginConfigurationRequest) {
        delegate.setBucketCrossOriginConfiguration(setBucketCrossOriginConfigurationRequest);
    }

    @Override
    public void deleteBucketCrossOriginConfiguration(String bucketName) {
        delegate.deleteBucketCrossOriginConfiguration(bucketName);
    }

    @Override
    public void deleteBucketCrossOriginConfiguration(DeleteBucketCrossOriginConfigurationRequest deleteBucketCrossOriginConfigurationRequest) {
        delegate.deleteBucketCrossOriginConfiguration(deleteBucketCrossOriginConfigurationRequest);
    }

    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(String bucketName) {
        return delegate.getBucketTaggingConfiguration(bucketName);
    }

    @Override
    public void setBucketTaggingConfiguration(String bucketName, BucketTaggingConfiguration bucketTaggingConfiguration) {
        delegate.setBucketTaggingConfiguration(bucketName, bucketTaggingConfiguration);
    }

    @Override
    public void setBucketTaggingConfiguration(SetBucketTaggingConfigurationRequest setBucketTaggingConfigurationRequest) {
        delegate.setBucketTaggingConfiguration(setBucketTaggingConfigurationRequest);
    }

    @Override
    public void deleteBucketTaggingConfiguration(String bucketName) {
        delegate.deleteBucketTaggingConfiguration(bucketName);
    }

    @Override
    public void deleteBucketTaggingConfiguration(DeleteBucketTaggingConfigurationRequest deleteBucketTaggingConfigurationRequest) {
        delegate.deleteBucketTaggingConfiguration(deleteBucketTaggingConfigurationRequest);
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketNotificationConfiguration(bucketName);
    }

    @Override
    public void setBucketNotificationConfiguration(SetBucketNotificationConfigurationRequest setBucketNotificationConfigurationRequest) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketNotificationConfiguration(setBucketNotificationConfigurationRequest);
    }

    @Override
    public void setBucketNotificationConfiguration(String bucketName, BucketNotificationConfiguration bucketNotificationConfiguration) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketNotificationConfiguration(bucketName, bucketNotificationConfiguration);
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketWebsiteConfiguration(bucketName);
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(GetBucketWebsiteConfigurationRequest getBucketWebsiteConfigurationRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketWebsiteConfiguration(getBucketWebsiteConfigurationRequest);
    }

    @Override
    public void setBucketWebsiteConfiguration(String bucketName, BucketWebsiteConfiguration configuration) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketWebsiteConfiguration(bucketName, configuration);
    }

    @Override
    public void setBucketWebsiteConfiguration(SetBucketWebsiteConfigurationRequest setBucketWebsiteConfigurationRequest) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketWebsiteConfiguration(setBucketWebsiteConfigurationRequest);
    }

    @Override
    public void deleteBucketWebsiteConfiguration(String bucketName) throws AmazonClientException, AmazonServiceException {
        delegate.deleteBucketWebsiteConfiguration(bucketName);
    }

    @Override
    public void deleteBucketWebsiteConfiguration(DeleteBucketWebsiteConfigurationRequest deleteBucketWebsiteConfigurationRequest) throws AmazonClientException, AmazonServiceException {
        delegate.deleteBucketWebsiteConfiguration(deleteBucketWebsiteConfigurationRequest);
    }

    @Override
    public BucketPolicy getBucketPolicy(String bucketName) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketPolicy(bucketName);
    }

    @Override
    public BucketPolicy getBucketPolicy(GetBucketPolicyRequest getBucketPolicyRequest) throws AmazonClientException, AmazonServiceException {
        return delegate.getBucketPolicy(getBucketPolicyRequest);
    }

    @Override
    public void setBucketPolicy(String bucketName, String policyText) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketPolicy(bucketName, policyText);
    }

    @Override
    public void setBucketPolicy(SetBucketPolicyRequest setBucketPolicyRequest) throws AmazonClientException, AmazonServiceException {
        delegate.setBucketPolicy(setBucketPolicyRequest);
    }

    @Override
    public void deleteBucketPolicy(String bucketName) throws AmazonClientException, AmazonServiceException {
        delegate.deleteBucketPolicy(bucketName);
    }

    @Override
    public void deleteBucketPolicy(DeleteBucketPolicyRequest deleteBucketPolicyRequest) throws AmazonClientException, AmazonServiceException {
        delegate.deleteBucketPolicy(deleteBucketPolicyRequest);
    }

    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration) throws AmazonClientException {
        return delegate.generatePresignedUrl(bucketName, key, expiration);
    }

    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration, HttpMethod method) throws AmazonClientException {
        return delegate.generatePresignedUrl(bucketName, key, expiration, method);
    }

    @Override
    public URL generatePresignedUrl(GeneratePresignedUrlRequest generatePresignedUrlRequest) throws AmazonClientException {
        return delegate.generatePresignedUrl(generatePresignedUrlRequest);
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadRequest request) throws AmazonClientException, AmazonServiceException {
        return delegate.initiateMultipartUpload(request);
    }

    @Override
    public UploadPartResult uploadPart(UploadPartRequest request) throws AmazonClientException, AmazonServiceException {
        return delegate.uploadPart(request);
    }

    @Override
    public PartListing listParts(ListPartsRequest request) throws AmazonClientException, AmazonServiceException {
        return delegate.listParts(request);
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest request) throws AmazonClientException, AmazonServiceException {
        delegate.abortMultipartUpload(request);
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request) throws AmazonClientException, AmazonServiceException {
        return delegate.completeMultipartUpload(request);
    }

    @Override
    public MultipartUploadListing listMultipartUploads(ListMultipartUploadsRequest request) throws AmazonClientException, AmazonServiceException {
        return delegate.listMultipartUploads(request);
    }

    @Override
    public S3ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
        return delegate.getCachedResponseMetadata(request);
    }

    @Override
    public void restoreObject(RestoreObjectRequest copyGlacierObjectRequest) throws AmazonServiceException {
        delegate.restoreObject(copyGlacierObjectRequest);
    }

    @Override
    public void restoreObject(String bucketName, String key, int expirationInDays) throws AmazonServiceException {
        delegate.restoreObject(bucketName, key, expirationInDays);
    }

    @Override
    public void enableRequesterPays(String bucketName) throws AmazonServiceException, AmazonClientException {
        delegate.enableRequesterPays(bucketName);
    }

    @Override
    public void disableRequesterPays(String bucketName) throws AmazonServiceException, AmazonClientException {
        delegate.disableRequesterPays(bucketName);
    }

    @Override
    public boolean isRequesterPaysEnabled(String bucketName) throws AmazonServiceException, AmazonClientException {
        return delegate.isRequesterPaysEnabled(bucketName);
    }


	@Override
	public ObjectListing listNextBatchOfObjects(ListNextBatchOfObjectsRequest listNextBatchOfObjectsRequest)
			throws AmazonClientException, AmazonServiceException {
		return delegate.listNextBatchOfObjects(listNextBatchOfObjectsRequest);
	}


	@Override
	public VersionListing listNextBatchOfVersions(ListNextBatchOfVersionsRequest listNextBatchOfVersionsRequest)
			throws AmazonClientException, AmazonServiceException {
		return delegate.listNextBatchOfVersions(listNextBatchOfVersionsRequest);
	}


	@Override
	public Owner getS3AccountOwner(GetS3AccountOwnerRequest getS3AccountOwnerRequest)
			throws AmazonClientException, AmazonServiceException {
		return delegate.getS3AccountOwner(getS3AccountOwnerRequest);
	}


	@Override
	public BucketLoggingConfiguration getBucketLoggingConfiguration(
			GetBucketLoggingConfigurationRequest getBucketLoggingConfigurationRequest)
					throws AmazonClientException, AmazonServiceException {
		return delegate.getBucketLoggingConfiguration(getBucketLoggingConfigurationRequest);
	}


	@Override
	public BucketVersioningConfiguration getBucketVersioningConfiguration(
			GetBucketVersioningConfigurationRequest getBucketVersioningConfigurationRequest)
					throws AmazonClientException, AmazonServiceException {
		return delegate.getBucketVersioningConfiguration(getBucketVersioningConfigurationRequest);
	}


	@Override
	public BucketLifecycleConfiguration getBucketLifecycleConfiguration(
			GetBucketLifecycleConfigurationRequest getBucketLifecycleConfigurationRequest) {
		return delegate.getBucketLifecycleConfiguration(getBucketLifecycleConfigurationRequest);
	}


	@Override
	public BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(
			GetBucketCrossOriginConfigurationRequest getBucketCrossOriginConfigurationRequest) {
		return delegate.getBucketCrossOriginConfiguration(getBucketCrossOriginConfigurationRequest);
	}


	@Override
	public BucketTaggingConfiguration getBucketTaggingConfiguration(
			GetBucketTaggingConfigurationRequest getBucketTaggingConfigurationRequest) {
		return delegate.getBucketTaggingConfiguration(getBucketTaggingConfigurationRequest);
	}


	@Override
	public BucketNotificationConfiguration getBucketNotificationConfiguration(
			GetBucketNotificationConfigurationRequest getBucketNotificationConfigurationRequest)
					throws AmazonClientException, AmazonServiceException {
		return delegate.getBucketNotificationConfiguration(getBucketNotificationConfigurationRequest);
	}


	@Override
	public BucketReplicationConfiguration getBucketReplicationConfiguration(
			GetBucketReplicationConfigurationRequest getBucketReplicationConfigurationRequest)
					throws AmazonServiceException, AmazonClientException {
		return delegate.getBucketReplicationConfiguration(getBucketReplicationConfigurationRequest);
	}


    @Override
    public ListObjectsV2Result listObjectsV2(String bucketName) throws SdkClientException, AmazonServiceException {
        return delegate.listObjectsV2(bucketName);
    }


    @Override
    public ListObjectsV2Result listObjectsV2(String bucketName, String prefix)
            throws SdkClientException, AmazonServiceException {
        return delegate.listObjectsV2(bucketName, prefix);
    }


    @Override
    public ListObjectsV2Result listObjectsV2(ListObjectsV2Request listObjectsV2Request)
            throws SdkClientException, AmazonServiceException {
        return delegate.listObjectsV2(listObjectsV2Request);
    }


    @Override
    public boolean doesBucketExistV2(String bucketName) throws SdkClientException, AmazonServiceException {
        return delegate.doesBucketExistV2(bucketName);
    }


    @Override
    public String getObjectAsString(String bucketName, String key) throws AmazonServiceException, SdkClientException {
        return delegate.getObjectAsString(bucketName, key);
    }


    @Override
    public GetObjectTaggingResult getObjectTagging(GetObjectTaggingRequest getObjectTaggingRequest) {
        return delegate.getObjectTagging(getObjectTaggingRequest);
    }


    @Override
    public SetObjectTaggingResult setObjectTagging(SetObjectTaggingRequest setObjectTaggingRequest) {
        return delegate.setObjectTagging(setObjectTaggingRequest);
    }


    @Override
    public DeleteObjectTaggingResult deleteObjectTagging(DeleteObjectTaggingRequest deleteObjectTaggingRequest) {
        return delegate.deleteObjectTagging(deleteObjectTaggingRequest);
    }


    @Override
    public PutObjectResult putObject(String bucketName, String key, String content)
            throws AmazonServiceException, SdkClientException {
        return delegate.putObject(bucketName, key, content);
    }


    @Override
    public RestoreObjectResult restoreObjectV2(RestoreObjectRequest request) throws AmazonServiceException {
        return delegate.restoreObjectV2(request);
    }


    @Override
    public void setRequestPaymentConfiguration(
            SetRequestPaymentConfigurationRequest setRequestPaymentConfigurationRequest) {
        delegate.setRequestPaymentConfiguration(setRequestPaymentConfigurationRequest);
    }


    @Override
    public BucketAccelerateConfiguration getBucketAccelerateConfiguration(String bucketName)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketAccelerateConfiguration(bucketName);
    }


    @Override
    public BucketAccelerateConfiguration getBucketAccelerateConfiguration(
            GetBucketAccelerateConfigurationRequest getBucketAccelerateConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketAccelerateConfiguration(getBucketAccelerateConfigurationRequest);
    }


    @Override
    public void setBucketAccelerateConfiguration(String bucketName,
            BucketAccelerateConfiguration accelerateConfiguration) throws AmazonServiceException, SdkClientException {
        delegate.setBucketAccelerateConfiguration(bucketName, accelerateConfiguration);
    }


    @Override
    public void setBucketAccelerateConfiguration(
            SetBucketAccelerateConfigurationRequest setBucketAccelerateConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        delegate.setBucketAccelerateConfiguration(setBucketAccelerateConfigurationRequest);
    }


    @Override
    public DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(String bucketName, String id)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketMetricsConfiguration(bucketName, id);
    }


    @Override
    public DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(
            DeleteBucketMetricsConfigurationRequest deleteBucketMetricsConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketMetricsConfiguration(deleteBucketMetricsConfigurationRequest);
    }


    @Override
    public GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(String bucketName, String id)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketMetricsConfiguration(bucketName, id);
    }


    @Override
    public GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(
            GetBucketMetricsConfigurationRequest getBucketMetricsConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketMetricsConfiguration(getBucketMetricsConfigurationRequest);
    }


    @Override
    public SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(String bucketName,
            MetricsConfiguration metricsConfiguration) throws AmazonServiceException, SdkClientException {
        return delegate.setBucketMetricsConfiguration(bucketName, metricsConfiguration);
    }


    @Override
    public SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(
            SetBucketMetricsConfigurationRequest setBucketMetricsConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.setBucketMetricsConfiguration(setBucketMetricsConfigurationRequest);
    }


    @Override
    public ListBucketMetricsConfigurationsResult listBucketMetricsConfigurations(
            ListBucketMetricsConfigurationsRequest listBucketMetricsConfigurationsRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.listBucketMetricsConfigurations(listBucketMetricsConfigurationsRequest);
    }


    @Override
    public DeleteBucketOwnershipControlsResult deleteBucketOwnershipControls(
            DeleteBucketOwnershipControlsRequest deleteBucketOwnershipControlsRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketOwnershipControls(deleteBucketOwnershipControlsRequest);
    }


    @Override
    public GetBucketOwnershipControlsResult getBucketOwnershipControls(
            GetBucketOwnershipControlsRequest getBucketOwnershipControlsRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketOwnershipControls(getBucketOwnershipControlsRequest);
    }


    @Override
    public SetBucketOwnershipControlsResult setBucketOwnershipControls(String bucketName,
            OwnershipControls ownershipControls) throws AmazonServiceException, SdkClientException {
        return delegate.setBucketOwnershipControls(bucketName, ownershipControls);
    }


    @Override
    public SetBucketOwnershipControlsResult setBucketOwnershipControls(
            SetBucketOwnershipControlsRequest setBucketOwnershipControlsRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.setBucketOwnershipControls(setBucketOwnershipControlsRequest);
    }


    @Override
    public DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(String bucketName, String id)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketAnalyticsConfiguration(bucketName, id);
    }


    @Override
    public DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(
            DeleteBucketAnalyticsConfigurationRequest deleteBucketAnalyticsConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketAnalyticsConfiguration(deleteBucketAnalyticsConfigurationRequest);
    }


    @Override
    public GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(String bucketName, String id)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketAnalyticsConfiguration(bucketName, id);
    }


    @Override
    public GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(
            GetBucketAnalyticsConfigurationRequest getBucketAnalyticsConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketAnalyticsConfiguration(getBucketAnalyticsConfigurationRequest);
    }


    @Override
    public SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(String bucketName,
            AnalyticsConfiguration analyticsConfiguration) throws AmazonServiceException, SdkClientException {
        return delegate.setBucketAnalyticsConfiguration(bucketName, analyticsConfiguration);
    }


    @Override
    public SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(
            SetBucketAnalyticsConfigurationRequest setBucketAnalyticsConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.setBucketAnalyticsConfiguration(setBucketAnalyticsConfigurationRequest);
    }


    @Override
    public ListBucketAnalyticsConfigurationsResult listBucketAnalyticsConfigurations(
            ListBucketAnalyticsConfigurationsRequest listBucketAnalyticsConfigurationsRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.listBucketAnalyticsConfigurations(listBucketAnalyticsConfigurationsRequest);
    }


    @Override
    public DeleteBucketIntelligentTieringConfigurationResult deleteBucketIntelligentTieringConfiguration(
            String bucketName, String id) throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketIntelligentTieringConfiguration(bucketName, id);
    }


    @Override
    public DeleteBucketIntelligentTieringConfigurationResult deleteBucketIntelligentTieringConfiguration(
            DeleteBucketIntelligentTieringConfigurationRequest deleteBucketIntelligentTieringConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketIntelligentTieringConfiguration(deleteBucketIntelligentTieringConfigurationRequest);
    }


    @Override
    public GetBucketIntelligentTieringConfigurationResult getBucketIntelligentTieringConfiguration(String bucketName,
            String id) throws AmazonServiceException, SdkClientException {
        return delegate.getBucketIntelligentTieringConfiguration(bucketName, id);
    }


    @Override
    public GetBucketIntelligentTieringConfigurationResult getBucketIntelligentTieringConfiguration(
            GetBucketIntelligentTieringConfigurationRequest getBucketIntelligentTieringConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketIntelligentTieringConfiguration(getBucketIntelligentTieringConfigurationRequest);
    }


    @Override
    public SetBucketIntelligentTieringConfigurationResult setBucketIntelligentTieringConfiguration(String bucketName,
            IntelligentTieringConfiguration intelligentTieringConfiguration)
            throws AmazonServiceException, SdkClientException {
        return delegate.setBucketIntelligentTieringConfiguration(bucketName, intelligentTieringConfiguration);
    }


    @Override
    public SetBucketIntelligentTieringConfigurationResult setBucketIntelligentTieringConfiguration(
            SetBucketIntelligentTieringConfigurationRequest setBucketIntelligentTieringConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.setBucketIntelligentTieringConfiguration(setBucketIntelligentTieringConfigurationRequest);
    }


    @Override
    public ListBucketIntelligentTieringConfigurationsResult listBucketIntelligentTieringConfigurations(
            ListBucketIntelligentTieringConfigurationsRequest listBucketIntelligentTieringConfigurationsRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.listBucketIntelligentTieringConfigurations(listBucketIntelligentTieringConfigurationsRequest);
    }


    @Override
    public DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(String bucketName, String id)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketInventoryConfiguration(bucketName, id);
    }


    @Override
    public DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(
            DeleteBucketInventoryConfigurationRequest deleteBucketInventoryConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketInventoryConfiguration(deleteBucketInventoryConfigurationRequest);
    }


    @Override
    public GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(String bucketName, String id)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketInventoryConfiguration(bucketName, id);
    }


    @Override
    public GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(
            GetBucketInventoryConfigurationRequest getBucketInventoryConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketInventoryConfiguration(getBucketInventoryConfigurationRequest);
    }


    @Override
    public SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(String bucketName,
            InventoryConfiguration inventoryConfiguration) throws AmazonServiceException, SdkClientException {
        return delegate.setBucketInventoryConfiguration(bucketName, inventoryConfiguration);
    }


    @Override
    public SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(
            SetBucketInventoryConfigurationRequest setBucketInventoryConfigurationRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.setBucketInventoryConfiguration(setBucketInventoryConfigurationRequest);
    }


    @Override
    public ListBucketInventoryConfigurationsResult listBucketInventoryConfigurations(
            ListBucketInventoryConfigurationsRequest listBucketInventoryConfigurationsRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.listBucketInventoryConfigurations(listBucketInventoryConfigurationsRequest);
    }


    @Override
    public DeleteBucketEncryptionResult deleteBucketEncryption(String bucketName)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketEncryption(bucketName);
    }


    @Override
    public DeleteBucketEncryptionResult deleteBucketEncryption(DeleteBucketEncryptionRequest request)
            throws AmazonServiceException, SdkClientException {
        return delegate.deleteBucketEncryption(request);
    }


    @Override
    public GetBucketEncryptionResult getBucketEncryption(String bucketName)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketEncryption(bucketName);
    }


    @Override
    public GetBucketEncryptionResult getBucketEncryption(GetBucketEncryptionRequest request)
            throws AmazonServiceException, SdkClientException {
        return delegate.getBucketEncryption(request);
    }


    @Override
    public SetBucketEncryptionResult setBucketEncryption(SetBucketEncryptionRequest setBucketEncryptionRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.setBucketEncryption(setBucketEncryptionRequest);
    }


    @Override
    public SetPublicAccessBlockResult setPublicAccessBlock(SetPublicAccessBlockRequest request) {
        return delegate.setPublicAccessBlock(request);
    }


    @Override
    public GetPublicAccessBlockResult getPublicAccessBlock(GetPublicAccessBlockRequest request) {
        return delegate.getPublicAccessBlock(request);
    }


    @Override
    public DeletePublicAccessBlockResult deletePublicAccessBlock(DeletePublicAccessBlockRequest request) {
        return delegate.deletePublicAccessBlock(request);
    }


    @Override
    public GetBucketPolicyStatusResult getBucketPolicyStatus(GetBucketPolicyStatusRequest request) {
        return delegate.getBucketPolicyStatus(request);
    }


    @Override
    public SelectObjectContentResult selectObjectContent(SelectObjectContentRequest selectRequest)
            throws AmazonServiceException, SdkClientException {
        return delegate.selectObjectContent(selectRequest);
    }


    @Override
    public SetObjectLegalHoldResult setObjectLegalHold(SetObjectLegalHoldRequest setObjectLegalHoldRequest) {
        return delegate.setObjectLegalHold(setObjectLegalHoldRequest);
    }


    @Override
    public GetObjectLegalHoldResult getObjectLegalHold(GetObjectLegalHoldRequest getObjectLegalHoldRequest) {
        return delegate.getObjectLegalHold(getObjectLegalHoldRequest);
    }


    @Override
    public SetObjectLockConfigurationResult setObjectLockConfiguration(
            SetObjectLockConfigurationRequest setObjectLockConfigurationRequest) {
        return delegate.setObjectLockConfiguration(setObjectLockConfigurationRequest);
    }


    @Override
    public GetObjectLockConfigurationResult getObjectLockConfiguration(
            GetObjectLockConfigurationRequest getObjectLockConfigurationRequest) {
        return delegate.getObjectLockConfiguration(getObjectLockConfigurationRequest);
    }


    @Override
    public SetObjectRetentionResult setObjectRetention(SetObjectRetentionRequest setObjectRetentionRequest) {
        return delegate.setObjectRetention(setObjectRetentionRequest);
    }


    @Override
    public GetObjectRetentionResult getObjectRetention(GetObjectRetentionRequest getObjectRetentionRequest) {
        return delegate.getObjectRetention(getObjectRetentionRequest);
    }


    @Override
    public WriteGetObjectResponseResult writeGetObjectResponse(
            WriteGetObjectResponseRequest writeGetObjectResponseRequest) {
        return delegate.writeGetObjectResponse(writeGetObjectResponseRequest);
    }


    @Override
    public PresignedUrlDownloadResult download(PresignedUrlDownloadRequest presignedUrlDownloadRequest) {
        return delegate.download(presignedUrlDownloadRequest);
    }


    @Override
    public void download(PresignedUrlDownloadRequest presignedUrlDownloadRequest, File destinationFile) {
        delegate.download(presignedUrlDownloadRequest, destinationFile);
    }


    @Override
    public PresignedUrlUploadResult upload(PresignedUrlUploadRequest presignedUrlUploadRequest) {
        return delegate.upload(presignedUrlUploadRequest);
    }


    @Override
    public void shutdown() {
        delegate.shutdown();        
    }


    @Override
    public com.amazonaws.services.s3.model.Region getRegion() {
        return delegate.getRegion();
    }


    @Override
    public String getRegionName() {
        return delegate.getRegionName();
    }


    @Override
    public URL getUrl(String bucketName, String key) {
        return delegate.getUrl(bucketName, key);
    }


    @Override
    public AmazonS3Waiters waiters() {
        return delegate.waiters();
    }
}
