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
package io.gravitee.reporter.elasticsearch.mapping.os;

import io.gravitee.elasticsearch.utils.Type;
import io.gravitee.reporter.elasticsearch.config.PipelineConfiguration;
import io.gravitee.reporter.elasticsearch.mapping.PerTypeIndexPreparer;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.functions.Function;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author GraviteeSource Team
 */
public class OsIndexPreparer extends PerTypeIndexPreparer {

    /**
     * Configuration of pipelineConfiguration
     */
    @Autowired
    private PipelineConfiguration pipelineConfiguration;

    @Override
    public Completable prepare() {
        if (configuration.isManagedIndex()) {
            return prepareIsmPolicy().andThen(indexMapping()).andThen(pipeline());
        }
        return indexMapping().andThen(pipeline());
    }

    @Override
    protected Function<Type, CompletableSource> indexTypeMapper() {
        return type -> {
            final String typeName = type.getType();
            final String templateName = configuration.getIndexName() + '-' + typeName;
            final String aliasName = configuration.getIndexName() + '-' + typeName;

            Map<String, Object> data = getTemplateData();
            logger.debug("Trying to put template mapping for type[{}] name[{}]", typeName, templateName);
            data.put("indexName", configuration.getIndexName() + '-' + typeName);
            data.put("indexesPrefix", configuration.getIndexName());

            final String template = freeMarkerComponent.generateFromTemplate("/os/mapping/index-template-" + typeName + ".ftl", data);
            if (configuration.isManagedIndex()) {
                return client.putTemplate(templateName, template).andThen(Completable.defer(() -> ensureAlias(aliasName)));
            }
            return client.putTemplate(templateName, template);
        };
    }

    private Completable prepareIsmPolicy() {
        final String indexesPrefix = configuration.getIndexName();
        Map<String, Object> data = getTemplateData();
        data.put("indexesPrefix", indexesPrefix);
        data.put("indexLifecycleMinSize", configuration.getIndexLifecycleMinSize());
        data.put("indexLifecycleMinIndexAge", configuration.getIndexLifecycleMinIndexAge());
        return createPolicyIfAbsent(indexesPrefix, freeMarkerComponent.generateFromTemplate("/os/policies/index-policy.ftl", data));
    }

    private Completable createPolicyIfAbsent(String policyName, String policy) {
        return client
            .getPolicy(policyName)
            .flatMapCompletable(policyJson ->
                client.createOrUpdatePolicy(
                    policyName,
                    policy,
                    policyJson.get("_seq_no").toString(),
                    policyJson.get("_primary_term").toString()
                )
            )
            .onErrorResumeNext(throwable -> client.createOrUpdatePolicy(policyName, policy, null, null));
    }

    private Completable ensureAlias(String aliasName) {
        final String aliasTemplate = freeMarkerComponent.generateFromTemplate(
            "/os/alias/alias.ftl",
            Collections.singletonMap("aliasName", aliasName)
        );

        return client
            .getAlias(aliasName)
            .switchIfEmpty(client.createIndexWithAlias(aliasName + "-000001", aliasTemplate).toMaybe())
            .ignoreElement();
    }

    private Completable pipeline() {
        String pipelineTemplate = pipelineConfiguration.createPipeline();

        if (pipelineTemplate != null && pipelineConfiguration.getPipelineName() != null) {
            return client
                .putPipeline(pipelineConfiguration.getPipelineName(), pipelineTemplate)
                .doOnComplete(() -> pipelineConfiguration.valid());
        }

        return Completable.complete();
    }
}
