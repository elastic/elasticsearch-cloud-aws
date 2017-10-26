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

package org.elasticsearch.cloud.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.*;
import com.amazonaws.http.IdleConnectionReaper;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class InternalAwsS3Service extends AbstractLifecycleComponent<AwsS3Service> implements AwsS3Service {

    /**
     * (acceskey, endpoint) -> client
     */
    private Map<Tuple<String, String>, AmazonS3Client> clients = new HashMap<Tuple<String,String>, AmazonS3Client>();

    @Inject
    public InternalAwsS3Service(Settings settings, SettingsFilter settingsFilter) {
        super(settings);

        settingsFilter.addFilter(new AwsSettingsFilter());
    }

    @Override
    public synchronized AmazonS3 client(String endpoint, String protocol, String region, String account, String key, Integer maxRetries,
                                        boolean useThrottleRetries, Boolean pathStyleAccess) {
        if (region != null && endpoint == null) {
            endpoint = getEndpoint(region);
            logger.debug("using s3 region [{}], with endpoint [{}]", region, endpoint);
        } else if (endpoint == null) {
            endpoint = getDefaultEndpoint();
        }
        if (account == null || key == null) {
            account = componentSettings.get("access_key", settings.get("cloud.account"));
            key = componentSettings.get("secret_key", settings.get("cloud.key"));
        }

        return getClient(endpoint, protocol, account, key, maxRetries, useThrottleRetries, pathStyleAccess);
    }


    private synchronized AmazonS3 getClient(String endpoint, String protocol, String account, String key, Integer maxRetries,
                                            boolean useThrottleRetries, Boolean pathStyleAccess) {
        Tuple<String, String> clientDescriptor = new Tuple<String, String>(endpoint, account);
        AmazonS3Client client = clients.get(clientDescriptor);
        if (client != null) {
            return client;
        }

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        // the response metadata cache is only there for diagnostics purposes,
        // but can force objects from every response to the old generation.
        clientConfiguration.setResponseMetadataCacheSize(0);
        if (protocol == null) {
            protocol = componentSettings.get("protocol", "https").toLowerCase();
            protocol = componentSettings.get("s3.protocol", protocol).toLowerCase();
        }

        if ("http".equals(protocol)) {
            clientConfiguration.setProtocol(Protocol.HTTP);
        } else if ("https".equals(protocol)) {
            clientConfiguration.setProtocol(Protocol.HTTPS);
        } else {
            throw new ElasticsearchIllegalArgumentException("No protocol supported [" + protocol + "], can either be [http] or [https]");
        }

        String proxyHost = settings.get("cloud.aws.proxy_host");
        proxyHost = settings.get("cloud.aws.s3.proxy_host", proxyHost);
        if (proxyHost != null) {
            String portString = settings.get("cloud.aws.proxy_port", "80");
            portString = settings.get("cloud.aws.s3.proxy_port", portString);
            Integer proxyPort;
            try {
                proxyPort = Integer.parseInt(portString, 10);
            } catch (NumberFormatException ex) {
                throw new ElasticsearchIllegalArgumentException("The configured proxy port value [" + portString + "] is invalid", ex);
            }
            clientConfiguration.withProxyHost(proxyHost).setProxyPort(proxyPort);
        }

        if (maxRetries != null) {
            // If not explicitly set, default to 3 with exponential backoff policy
            clientConfiguration.setMaxErrorRetry(maxRetries);
        }
        clientConfiguration.setUseThrottleRetries(useThrottleRetries);

        // #155: we might have 3rd party users using older S3 API version
        String awsSigner = settings.get("cloud.aws.s3.signer", settings.get("cloud.aws.signer"));
        if (awsSigner != null) {
            logger.debug("using AWS API signer [{}]", awsSigner);
            try {
                AwsSigner.configureSigner(awsSigner, clientConfiguration);
            } catch (ElasticsearchIllegalArgumentException e) {
                logger.warn("wrong signer set for [cloud.aws.s3.signer] or [cloud.aws.signer]: [{}]", awsSigner);
            }
        }

        AWSCredentialsProvider credentials;

        if (account == null && key == null) {
            credentials = new AWSCredentialsProviderChain(
                    new EnvironmentVariableCredentialsProvider(),
                    new SystemPropertiesCredentialsProvider(),
                    new InstanceProfileCredentialsProvider()
            );
        } else {
            credentials = new AWSCredentialsProviderChain(
                    new StaticCredentialsProvider(new BasicAWSCredentials(account, key))
            );
        }
        client = new AmazonS3Client(credentials, clientConfiguration);

        if (endpoint != null) {
            client.setEndpoint(endpoint);
        }
        
        if(pathStyleAccess != null) {
        	client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(pathStyleAccess).build());
        }
        
        clients.put(clientDescriptor, client);
        return client;
    }

    private String getDefaultEndpoint() {
        String endpoint = null;
        if (componentSettings.get("s3.endpoint") != null) {
            endpoint = componentSettings.get("s3.endpoint");
            logger.debug("using explicit s3 endpoint [{}]", endpoint);
        } else if (componentSettings.get("region") != null) {
            String region = componentSettings.get("region").toLowerCase();
            endpoint = getEndpoint(region);
            logger.debug("using s3 region [{}], with endpoint [{}]", region, endpoint);
        }
        return endpoint;
    }

    private static String getEndpoint(String region) {
        if ("us-east".equals(region) || "us-east-1".equals(region)) {
            return "s3.amazonaws.com";
        } else if ("us-west".equals(region) || "us-west-1".equals(region)) {
            return "s3-us-west-1.amazonaws.com";
        } else if ("us-west-2".equals(region)) {
            return "s3-us-west-2.amazonaws.com";
        } else if ("ap-southeast".equals(region) || "ap-southeast-1".equals(region)) {
            return "s3-ap-southeast-1.amazonaws.com";
        } else if ("ap-southeast-2".equals(region)) {
            return "s3-ap-southeast-2.amazonaws.com";
        } else if ("ap-northeast".equals(region) || "ap-northeast-1".equals(region)) {
            return "s3-ap-northeast-1.amazonaws.com";
        } else if ("eu-west".equals(region) || "eu-west-1".equals(region)) {
            return "s3-eu-west-1.amazonaws.com";
        } else if ("eu-central".equals(region) || "eu-central-1".equals(region)) {
            return "s3.eu-central-1.amazonaws.com";
        } else if ("sa-east".equals(region) || "sa-east-1".equals(region)) {
            return "s3-sa-east-1.amazonaws.com";
        } else if ("cn-north".equals(region) || "cn-north-1".equals(region)) {
            return "s3.cn-north-1.amazonaws.com.cn";
        } else {
            throw new ElasticsearchIllegalArgumentException("No automatic endpoint could be derived from region [" + region + "]");
        }
    }

    @Override
    protected void doStart() throws ElasticsearchException {
    }

    @Override
    protected void doStop() throws ElasticsearchException {
    }

    @Override
    protected void doClose() throws ElasticsearchException {
        for (AmazonS3Client client : clients.values()) {
            client.shutdown();
        }

        // Ensure that IdleConnectionReaper is shutdown
        IdleConnectionReaper.shutdown();
    }
}
