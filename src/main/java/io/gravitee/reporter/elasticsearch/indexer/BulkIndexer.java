/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.reporter.elasticsearch.indexer;

import io.gravitee.elasticsearch.client.Client;
import io.gravitee.reporter.api.Reportable;
import io.gravitee.reporter.elasticsearch.config.ReporterConfiguration;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.buffer.Buffer;
import io.vertx.rxjava3.core.Vertx;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class BulkIndexer extends AbstractIndexer {

    /**
     * Elasticsearch client.
     */
    @Autowired
    private Client client;

    @Autowired
    private Vertx vertx;

    /**
     * Configuration of Elasticsearch (cluster name, addresses, ...)
     */
    @Autowired
    private ReporterConfiguration configuration;

    private final PublishProcessor<Reportable> bulkProcessor = PublishProcessor.create();

    @PostConstruct
    public void initialize() {
        bulkProcessor
            .onBackpressureBuffer()
            .observeOn(Schedulers.io())
            .flatMap(
                (Function<Reportable, Publisher<Buffer>>) reportable ->
                    Flowable.just(reportable).map(this::transform).onErrorResumeWith(Flowable.empty())
            )
            .buffer(configuration.getFlushInterval(), TimeUnit.SECONDS, configuration.getBulkActions())
            .filter(payload -> !payload.isEmpty())
            .subscribe(new DocumentBulkProcessor(client, vertx));
    }

    @Override
    public void index(Reportable reportable) {
        bulkProcessor.onNext(reportable);
    }
}
