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

package org.elasticsearch.repositories.s3;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.index.snapshots.IndexShardRepository;
import org.elasticsearch.index.snapshots.blobstore.BlobStoreIndexShardRepository;
import org.elasticsearch.repositories.Repository;

/**
 * S3 repository module
 */
public class S3RepositoryModule extends AbstractModule {

    public S3RepositoryModule() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(Repository.class).to(S3Repository.class).asEagerSingleton();
        bind(IndexShardRepository.class).to(BlobStoreIndexShardRepository.class).asEagerSingleton();
    }
}

