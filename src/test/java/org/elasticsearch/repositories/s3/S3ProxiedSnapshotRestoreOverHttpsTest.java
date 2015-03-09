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

import org.elasticsearch.common.settings.Settings;
import org.junit.Before;

/**
 * This will only run if you define in your `elasticsearch.yml` file a s3 specific proxy
 * cloud.aws.s3.proxy_host: mys3proxy.company.com
 * cloud.aws.s3.proxy_port: 8080
 */
public class S3ProxiedSnapshotRestoreOverHttpsTest extends AbstractS3SnapshotRestoreTest {

    private boolean proxySet = false;

    @Override
    public Settings nodeSettings(int nodeOrdinal) {
        Settings settings = super.nodeSettings(nodeOrdinal);
        String proxyHost = settings.get("cloud.aws.s3.proxy_host");
        proxySet = proxyHost != null;
        return settings;
    }

    @Before
    public void checkProxySettings() {
        assumeTrue("we are expecting proxy settings in elasticsearch.yml file", proxySet);
    }

}
