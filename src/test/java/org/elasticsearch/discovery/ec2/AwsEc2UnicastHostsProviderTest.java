/*
 * Licensed to Crate.IO GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package org.elasticsearch.discovery.ec2;

import com.amazonaws.services.ec2.AmazonEC2Client;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.TransportService;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AwsEc2UnicastHostsProviderTest {

    private TransportService transportService = mock(TransportService.class);
    private AmazonEC2Client client = mock(AmazonEC2Client.class);

    abstract class DummyEc2HostProvider extends AwsEc2UnicastHostsProvider {

        public int fetchCount = 0;

        public DummyEc2HostProvider(Settings settings, TransportService transportService, AmazonEC2Client client,  Version version) {
            super(settings, transportService, client, version);
        }

    }

    @Test
    public void testGetNodeListEmptyCache() throws Exception {
        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder();
        DummyEc2HostProvider provider = new DummyEc2HostProvider(builder.build(), transportService, client, Version.CURRENT) {
            @Override
            protected List<DiscoveryNode> fetchDynamicNodes() {
                fetchCount++;
                return Lists.newArrayList();
            }
        };
        for (int i=0; i<3; i++) {
            provider.buildDynamicNodes();
        }
        assertTrue(provider.fetchCount == 3);
    }

    @Test
    public void testGetNodeListCached() throws Exception {
        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
                .put("discovery.ec2.node_cache_time", "500ms");
        DummyEc2HostProvider provider = new DummyEc2HostProvider(builder.build(), transportService, client, Version.CURRENT) {
            @Override
            protected List<DiscoveryNode> fetchDynamicNodes() {
                fetchCount++;
                return Lists.newArrayList(mock(DiscoveryNode.class));
            }
        };
        for (int i=0; i<3; i++) {
            provider.buildDynamicNodes();
        }
        assertTrue(provider.fetchCount == 1);
        Thread.sleep(1_000L); // wait for cache to expire
        for (int i=0; i<3; i++) {
            provider.buildDynamicNodes();
        }
        assertTrue(provider.fetchCount == 2);
    }
}