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

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

/**
 * Requires the following system properties to be set:
 * <ul>
 * <li>s3.proxy_host--uri or ip address of the proxy host</li>
 * <li>s3.proxy_port--port proxy listens on (squid default is 3128)
 * </ul>
 */
public class S3ProxiedSnapshotRestoreOverHttpsTest extends AbstractS3SnapshotRestoreTest {
    @Override
    public Settings nodeSettings(int nodeOrdinal) {
        ImmutableSettings.Builder settings = ImmutableSettings.builder()
                .put(super.nodeSettings(nodeOrdinal))
                .put("cloud.aws.s3.protocol", "https")
                .put("cloud.aws.s3.proxy_host",System.getProperty("s3.proxy_host"))
                .put("cloud.aws.s3.proxy_port",System.getProperty("s3.proxy_port"));
        return settings.build();
    }
}
