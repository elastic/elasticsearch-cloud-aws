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

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.settings.Settings;

/**
 *
 */
public class AwsModule extends AbstractModule {

    private final Settings settings;

    public static final String S3_SERVICE_TYPE_KEY = "cloud.aws.s3service.type";

    public AwsModule(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(AwsS3Service.class).to(getS3ServiceClass(settings)).asEagerSingleton();
        bind(AwsEc2Service.class).asEagerSingleton();
    }

    public static Class<? extends AwsS3Service> getS3ServiceClass(Settings settings) {
        return settings.getAsClass(S3_SERVICE_TYPE_KEY, InternalAwsS3Service.class);
    }

}