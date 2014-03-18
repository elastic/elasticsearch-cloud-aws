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

package org.elasticsearch.repositories.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryResponse;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cloud.aws.AbstractAwsTest;
import org.elasticsearch.cloud.aws.AbstractAwsTest.AwsTest;
import org.elasticsearch.cloud.aws.AwsS3Service;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.elasticsearch.repositories.RepositoryMissingException;
import org.elasticsearch.snapshots.SnapshotState;
import org.elasticsearch.test.ElasticsearchIntegrationTest.ClusterScope;
import org.elasticsearch.test.ElasticsearchIntegrationTest.Scope;
import org.elasticsearch.test.store.MockDirectoryHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 */
@AwsTest
@ClusterScope(scope = Scope.SUITE, numNodes = 2)
public class S3SnapshotRestoreTest extends AbstractAwsTest {

    @Override
    public Settings indexSettings() {
        // During restore we frequently restore index to exactly the same state it was before, that might cause the same
        // checksum file to be written twice during restore operation
        return ImmutableSettings.builder().put(super.indexSettings())
                .put(MockDirectoryHelper.RANDOM_PREVENT_DOUBLE_WRITE, false)
                .put(MockDirectoryHelper.RANDOM_NO_DELETE_OPEN_FILE, false)
                .put("cloud.enabled", true)
                .build();
    }

    private String basePath;

    @Before
    public final void wipeBefore() {
        wipeRepositories();
        basePath = "repo-" + randomInt();
        cleanRepositoryFiles(basePath);
    }

    @After
    public final void wipeAfter() {
        wipeRepositories();
        cleanRepositoryFiles(basePath);
    }

    @Test
    public void testSimpleWorkflow() {
        Client client = client();
        logger.info("-->  creating s3 repository with bucket[{}] and path [{}]", cluster().getInstance(Settings.class).get("repositories.s3.bucket"), basePath);
        PutRepositoryResponse putRepositoryResponse = client.admin().cluster().preparePutRepository("test-repo")
                .setType("s3").setSettings(ImmutableSettings.settingsBuilder()
                        .put("base_path", basePath)
                        .put("chunk_size", randomIntBetween(1000, 10000))
                ).get();
        assertThat(putRepositoryResponse.isAcknowledged(), equalTo(true));

        createIndex("test-idx-1", "test-idx-2", "test-idx-3");
        ensureGreen();

        logger.info("--> indexing some data");
        for (int i = 0; i < 100; i++) {
            index("test-idx-1", "doc", Integer.toString(i), "foo", "bar" + i);
            index("test-idx-2", "doc", Integer.toString(i), "foo", "baz" + i);
            index("test-idx-3", "doc", Integer.toString(i), "foo", "baz" + i);
        }
        refresh();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(100L));
        assertThat(client.prepareCount("test-idx-2").get().getCount(), equalTo(100L));
        assertThat(client.prepareCount("test-idx-3").get().getCount(), equalTo(100L));

        logger.info("--> snapshot");
        CreateSnapshotResponse createSnapshotResponse = client.admin().cluster().prepareCreateSnapshot("test-repo", "test-snap").setWaitForCompletion(true).setIndices("test-idx-*", "-test-idx-3").get();
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), greaterThan(0));
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), equalTo(createSnapshotResponse.getSnapshotInfo().totalShards()));

        assertThat(client.admin().cluster().prepareGetSnapshots("test-repo").setSnapshots("test-snap").get().getSnapshots().get(0).state(), equalTo(SnapshotState.SUCCESS));

        logger.info("--> delete some data");
        for (int i = 0; i < 50; i++) {
            client.prepareDelete("test-idx-1", "doc", Integer.toString(i)).get();
        }
        for (int i = 50; i < 100; i++) {
            client.prepareDelete("test-idx-2", "doc", Integer.toString(i)).get();
        }
        for (int i = 0; i < 100; i += 2) {
            client.prepareDelete("test-idx-3", "doc", Integer.toString(i)).get();
        }
        refresh();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(50L));
        assertThat(client.prepareCount("test-idx-2").get().getCount(), equalTo(50L));
        assertThat(client.prepareCount("test-idx-3").get().getCount(), equalTo(50L));

        logger.info("--> close indices");
        client.admin().indices().prepareClose("test-idx-1", "test-idx-2").get();

        logger.info("--> restore all indices from the snapshot");
        RestoreSnapshotResponse restoreSnapshotResponse = client.admin().cluster().prepareRestoreSnapshot("test-repo", "test-snap").setWaitForCompletion(true).execute().actionGet();
        assertThat(restoreSnapshotResponse.getRestoreInfo().totalShards(), greaterThan(0));

        ensureGreen();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(100L));
        assertThat(client.prepareCount("test-idx-2").get().getCount(), equalTo(100L));
        assertThat(client.prepareCount("test-idx-3").get().getCount(), equalTo(50L));

        // Test restore after index deletion
        logger.info("--> delete indices");
        wipeIndices("test-idx-1", "test-idx-2");
        logger.info("--> restore one index after deletion");
        restoreSnapshotResponse = client.admin().cluster().prepareRestoreSnapshot("test-repo", "test-snap").setWaitForCompletion(true).setIndices("test-idx-*", "-test-idx-2").execute().actionGet();
        assertThat(restoreSnapshotResponse.getRestoreInfo().totalShards(), greaterThan(0));
        ensureGreen();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(100L));
        ClusterState clusterState = client.admin().cluster().prepareState().get().getState();
        assertThat(clusterState.getMetaData().hasIndex("test-idx-1"), equalTo(true));
        assertThat(clusterState.getMetaData().hasIndex("test-idx-2"), equalTo(false));
    }

    /**
     * This test verifies that the test configuration is set up in a manner that
     * does not make the test {@link #testRepositoryWithCustomCredentials()} pointless.
     */
    @Test(expected = UncategorizedExecutionException.class)
    public void assertRepositoryWithCustomCredentialsIsNotAccessibleByDefaultCredentials() {
        Client client = client();
        Settings bucketSettings = cluster().getInstance(Settings.class).getByPrefix("repositories.s3.private-bucket.");
        logger.info("-->  creating s3 repository with bucket[{}] and path [{}]", bucketSettings.get("bucket"), basePath);
        PutRepositoryResponse putRepositoryResponse = client.admin().cluster().preparePutRepository("test-repo")
                .setType("s3").setSettings(ImmutableSettings.settingsBuilder()
                        .put("base_path", basePath)
                        .put("bucket", bucketSettings.get("bucket"))
                ).get();
        assertThat(putRepositoryResponse.isAcknowledged(), equalTo(true));

        assertRepositoryIsOperational(client, "test-repo");
    }

    @Test
    public void testRepositoryWithCustomCredentials() {
        Client client = client();
        Settings bucketSettings = cluster().getInstance(Settings.class).getByPrefix("repositories.s3.private-bucket.");
        logger.info("-->  creating s3 repository with bucket[{}] and path [{}]", bucketSettings.get("bucket"), basePath);
        PutRepositoryResponse putRepositoryResponse = client.admin().cluster().preparePutRepository("test-repo")
                .setType("s3").setSettings(ImmutableSettings.settingsBuilder()
                        .put("base_path", basePath)
                        .put("access_key", bucketSettings.get("access_key"))
                        .put("secret_key", bucketSettings.get("secret_key"))
                        .put("bucket", bucketSettings.get("bucket"))
                ).get();
        assertThat(putRepositoryResponse.isAcknowledged(), equalTo(true));

        assertRepositoryIsOperational(client, "test-repo");
    }

    /**
     * This test verifies that the test configuration is set up in a manner that
     * does not make the test {@link #testRepositoryInRemoteRegion()} pointless.
     */
    @Test(expected = UncategorizedExecutionException.class)
    public void assertRepositoryInRemoteRegionIsRemote() {
        Client client = client();
        Settings bucketSettings = cluster().getInstance(Settings.class).getByPrefix("repositories.s3.remote-bucket.");
        logger.info("-->  creating s3 repository with bucket[{}] and path [{}]", bucketSettings.get("bucket"), basePath);
        PutRepositoryResponse putRepositoryResponse = client.admin().cluster().preparePutRepository("test-repo")
                .setType("s3").setSettings(ImmutableSettings.settingsBuilder()
                        .put("base_path", basePath)
                        .put("bucket", bucketSettings.get("bucket"))
// Below setting intentionally omitted to assert bucket is not available in default region.
//                        .put("region", privateBucketSettings.get("region"))
                ).get();
        assertThat(putRepositoryResponse.isAcknowledged(), equalTo(true));

        assertRepositoryIsOperational(client, "test-repo");
    }

    @Test
    public void testRepositoryInRemoteRegion() {
        Client client = client();
        Settings settings = cluster().getInstance(Settings.class);
        Settings bucketSettings = settings.getByPrefix("repositories.s3.remote-bucket.");
        logger.info("-->  creating s3 repository with bucket[{}] and path [{}]", bucketSettings.get("bucket"), basePath);
        PutRepositoryResponse putRepositoryResponse = client.admin().cluster().preparePutRepository("test-repo")
                .setType("s3").setSettings(ImmutableSettings.settingsBuilder()
                        .put("base_path", basePath)
                        .put("bucket", bucketSettings.get("bucket"))
                        .put("region", bucketSettings.get("region"))
                ).get();
        assertThat(putRepositoryResponse.isAcknowledged(), equalTo(true));

        assertRepositoryIsOperational(client, "test-repo");
    }

    private void assertRepositoryIsOperational(Client client, String repository) {
        createIndex("test-idx-1");
        ensureGreen();

        logger.info("--> indexing some data");
        for (int i = 0; i < 100; i++) {
            index("test-idx-1", "doc", Integer.toString(i), "foo", "bar" + i);
        }
        refresh();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(100L));

        logger.info("--> snapshot");
        CreateSnapshotResponse createSnapshotResponse = client.admin().cluster().prepareCreateSnapshot(repository, "test-snap").setWaitForCompletion(true).setIndices("test-idx-*").get();
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), greaterThan(0));
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), equalTo(createSnapshotResponse.getSnapshotInfo().totalShards()));

        assertThat(client.admin().cluster().prepareGetSnapshots(repository).setSnapshots("test-snap").get().getSnapshots().get(0).state(), equalTo(SnapshotState.SUCCESS));

        logger.info("--> delete some data");
        for (int i = 0; i < 50; i++) {
            client.prepareDelete("test-idx-1", "doc", Integer.toString(i)).get();
        }
        refresh();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(50L));

        logger.info("--> close indices");
        client.admin().indices().prepareClose("test-idx-1").get();

        logger.info("--> restore all indices from the snapshot");
        RestoreSnapshotResponse restoreSnapshotResponse = client.admin().cluster().prepareRestoreSnapshot(repository, "test-snap").setWaitForCompletion(true).execute().actionGet();
        assertThat(restoreSnapshotResponse.getRestoreInfo().totalShards(), greaterThan(0));

        ensureGreen();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(100L));
    }


    /**
     * Deletes repositories, supports wildcard notation.
     */
    public static void wipeRepositories(String... repositories) {
        // if nothing is provided, delete all
        if (repositories.length == 0) {
            repositories = new String[]{"*"};
        }
        for (String repository : repositories) {
            try {
                client().admin().cluster().prepareDeleteRepository(repository).execute().actionGet();
            } catch (RepositoryMissingException ex) {
                // ignore
            }
        }
    }

    /**
     * Deletes content of the repository files in the bucket
     */
    public void cleanRepositoryFiles(String basePath) {
        Settings settings = cluster().getInstance(Settings.class);
        Settings[] buckets = {
                settings.getByPrefix("repositories.s3."),
                settings.getByPrefix("repositories.s3.private-bucket."),
                settings.getByPrefix("repositories.s3.remote-bucket.")
            };
        for (Settings bucket : buckets) {
            AmazonS3 client = cluster().getInstance(AwsS3Service.class).client(
                    bucket.get("region", settings.get("repositories.s3.region")),
                    bucket.get("access_key", settings.get("cloud.aws.access_key")),
                    bucket.get("secret_key", settings.get("cloud.aws.secret_key")));
            try {
                ObjectListing prevListing = null;
                //From http://docs.amazonwebservices.com/AmazonS3/latest/dev/DeletingMultipleObjectsUsingJava.html
                //we can do at most 1K objects per delete
                //We don't know the bucket name until first object listing
                DeleteObjectsRequest multiObjectDeleteRequest = null;
                ArrayList<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<DeleteObjectsRequest.KeyVersion>();
                while (true) {
                    ObjectListing list;
                    if (prevListing != null) {
                        list = client.listNextBatchOfObjects(prevListing);
                    } else {
                        list = client.listObjects(bucket.get("bucket"), basePath);
                        multiObjectDeleteRequest = new DeleteObjectsRequest(list.getBucketName());
                    }
                    for (S3ObjectSummary summary : list.getObjectSummaries()) {
                        keys.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
                        //Every 500 objects batch the delete request
                        if (keys.size() > 500) {
                            multiObjectDeleteRequest.setKeys(keys);
                            client.deleteObjects(multiObjectDeleteRequest);
                            multiObjectDeleteRequest = new DeleteObjectsRequest(list.getBucketName());
                            keys.clear();
                        }
                    }
                    if (list.isTruncated()) {
                        prevListing = list;
                    } else {
                        break;
                    }
                }
                if (!keys.isEmpty()) {
                    multiObjectDeleteRequest.setKeys(keys);
                    client.deleteObjects(multiObjectDeleteRequest);
                }
            } catch (Throwable ex) {
                logger.warn("Failed to delete S3 repository", ex);
            }
        }
    }
}
