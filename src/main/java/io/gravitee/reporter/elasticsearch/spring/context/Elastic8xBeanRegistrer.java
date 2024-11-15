/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.reporter.elasticsearch.spring.context;

import io.gravitee.reporter.elasticsearch.config.ReporterConfiguration;
import io.gravitee.reporter.elasticsearch.indexer.AbstractIndexer;
import io.gravitee.reporter.elasticsearch.indexer.es8.ES8BulkIndexer;
import io.gravitee.reporter.elasticsearch.indexer.name.AbstractIndexNameGenerator;
import io.gravitee.reporter.elasticsearch.indexer.name.PerTypeAndDateIndexNameGenerator;
import io.gravitee.reporter.elasticsearch.indexer.name.PerTypeIndexNameGenerator;
import io.gravitee.reporter.elasticsearch.mapping.AbstractIndexPreparer;
import io.gravitee.reporter.elasticsearch.mapping.es8.ES8IndexPreparer;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class Elastic8xBeanRegistrer extends AbstractElasticBeanRegistrer {

    @Override
    protected Class<? extends AbstractIndexer> getIndexerClass() {
        return ES8BulkIndexer.class;
    }

    @Override
    protected Class<? extends AbstractIndexPreparer> getIndexPreparerClass(ReporterConfiguration configuration) {
        return ES8IndexPreparer.class;
    }

    @Override
    protected Class<? extends AbstractIndexNameGenerator> getIndexNameGeneratorClass(ReporterConfiguration configuration) {
        return configuration.isIlmManagedIndex() ? PerTypeIndexNameGenerator.class : PerTypeAndDateIndexNameGenerator.class;
    }
}
