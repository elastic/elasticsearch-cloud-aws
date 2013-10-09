/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
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

package org.elasticsearch.discovery.ec2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.ImmutableSet;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.discovery.zen.ping.unicast.UnicastHostsProvider;
import org.elasticsearch.discovery.zen.ping.unicast.UnicastZenPing;
import org.elasticsearch.transport.TransportService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class AwsEc2UnicastHostsProvider extends AbstractComponent implements UnicastHostsProvider {

    private static enum HostType {
        PRIVATE_IP,
        PUBLIC_IP,
        PRIVATE_DNS,
        PUBLIC_DNS
    }

    private final TransportService transportService;

    private final AmazonEC2 client;

    private final boolean bindAnyGroup;

    private final ImmutableSet<String> groups;

    private final ImmutableMap<String, String> tags;

    private final ImmutableSet<String> availabilityZones;

    private final HostType hostType;

    @Inject
    public AwsEc2UnicastHostsProvider(Settings settings, TransportService transportService, AmazonEC2 client) {
        super(settings);
        this.transportService = transportService;
        this.client = client;

        this.hostType = HostType.valueOf(componentSettings.get("host_type", "private_ip").toUpperCase());

        this.bindAnyGroup = componentSettings.getAsBoolean("any_group", true);
        this.groups = ImmutableSet.copyOf(componentSettings.getAsArray("groups"));

        this.tags = componentSettings.getByPrefix("tag.").getAsMap();

        Set<String> availabilityZones = Sets.newHashSet(componentSettings.getAsArray("availability_zones"));
        if (componentSettings.get("availability_zones") != null) {
            availabilityZones.addAll(Strings.commaDelimitedListToSet(componentSettings.get("availability_zones")));
        }
        this.availabilityZones = ImmutableSet.copyOf(availabilityZones);

        if (logger.isDebugEnabled()) {
            logger.debug("using host_type [{}], tags [{}], groups [{}] with any_group [{}], availability_zones [{}]", hostType, tags, groups, bindAnyGroup, availabilityZones);
        }
    }

    @Override
    public List<DiscoveryNode> buildDynamicNodes() {
        List<DiscoveryNode> discoNodes = Lists.newArrayList();

        DescribeInstancesResult descInstances;
        try {
            descInstances = client.describeInstances(new DescribeInstancesRequest());
        } catch (AmazonClientException e) {
            logger.info("Exception while retrieving instance list from AWS API: {}", e.getMessage());
            logger.debug("Full exception:", e);
            return discoNodes;
        }

        logger.trace("building dynamic unicast discovery nodes...");
        for (Reservation reservation : descInstances.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                if (!availabilityZones.isEmpty()) {
                    if (!availabilityZones.contains(instance.getPlacement().getAvailabilityZone())) {
                        logger.trace("filtering out instance {} based on availability_zone {}, not part of {}", instance.getInstanceId(), instance.getPlacement().getAvailabilityZone(), availabilityZones);
                        continue;
                    }
                }

                // lets see if we can filter based on groups
                if (!groups.isEmpty()) {
                    List<GroupIdentifier> instanceSecurityGroups = instance.getSecurityGroups();
                    ArrayList<String> securityGroupNames = new ArrayList<String>();
                    for (GroupIdentifier sg : instanceSecurityGroups) {
                        securityGroupNames.add(sg.getGroupName());
                    }
                    if (bindAnyGroup) {
                        if (Collections.disjoint(securityGroupNames, groups)) {
                            logger.trace("filtering out instance {} based on groups {}, not part of {}", instance.getInstanceId(), instanceSecurityGroups, groups);
                            // continue to the next instance
                            continue;
                        }
                    } else {
                        if (!securityGroupNames.containsAll(groups)) {
                            logger.trace("filtering out instance {} based on groups {}, does not include all of {}", instance.getInstanceId(), instanceSecurityGroups, groups);
                            // continue to the next instance
                            continue;
                        }
                    }
                }

                // see if we need to filter by tags
                boolean filterByTag = false;
                if (!tags.isEmpty()) {
                    if (instance.getTags() == null) {
                        filterByTag = true;
                    } else {
                        // check that all tags listed are there on the instance
                        for (Map.Entry<String, String> entry : tags.entrySet()) {
                            boolean found = false;
                            for (Tag tag : instance.getTags()) {
                                if (entry.getKey().equals(tag.getKey()) && entry.getValue().equals(tag.getValue())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                filterByTag = true;
                                break;
                            }
                        }
                    }
                }
                if (filterByTag) {
                    logger.trace("filtering out instance {} based tags {}, not part of {}", instance.getInstanceId(), tags, instance.getTags());
                    continue;
                }

                InstanceState state = instance.getState();
                if (state.getName().equalsIgnoreCase("pending") || state.getName().equalsIgnoreCase("running")) {
                    String address = null;
                    switch (hostType) {
                        case PRIVATE_DNS:
                            address = instance.getPrivateDnsName();
                            break;
                        case PRIVATE_IP:
                            address = instance.getPrivateIpAddress();
                            break;
                        case PUBLIC_DNS:
                            address = instance.getPublicDnsName();
                            break;
                        case PUBLIC_IP:
                            address = instance.getPublicDnsName();
                            break;
                    }
                    if (address != null) {
                        try {
                            TransportAddress[] addresses = transportService.addressesFromString(address);
                            // we only limit to 1 address, makes no sense to ping 100 ports
                            for (int i = 0; (i < addresses.length && i < UnicastZenPing.LIMIT_PORTS_COUNT); i++) {
                                logger.trace("adding {}, address {}, transport_address {}", instance.getInstanceId(), address, addresses[i]);
                                discoNodes.add(new DiscoveryNode("#cloud-" + instance.getInstanceId() + "-" + i, addresses[i], Version.CURRENT));
                            }
                        } catch (Exception e) {
                            logger.warn("failed to add {}, address {}", e, instance.getInstanceId(), address);
                        }
                    } else {
                        logger.trace("not adding {}, address is null, host_type {}", instance.getInstanceId(), hostType);
                    }
                } else {
                    logger.trace("not adding {}, state {} is not pending or running", instance.getInstanceId(), state.getName());
                }
            }
        }

        logger.debug("using dynamic discovery nodes {}", discoNodes);

        return discoNodes;
    }
}
