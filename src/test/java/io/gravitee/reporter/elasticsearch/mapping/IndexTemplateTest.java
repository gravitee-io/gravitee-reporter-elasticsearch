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
package io.gravitee.reporter.elasticsearch.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.common.templating.FreeMarkerComponent;
import io.gravitee.reporter.elasticsearch.config.PipelineConfiguration;
import io.gravitee.reporter.elasticsearch.config.ReporterConfiguration;
import io.gravitee.reporter.elasticsearch.mapping.es8.ES8IndexPreparer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Asserts what the index templates actually render: the configured lifecycle property names when they are
 * overridden, the Elasticsearch defaults when they are blank, and JSON-safe output when a name or policy is
 * malformed. Runs without the container {@code ElasticsearchReporterTest} needs.
 *
 * <p>Templates are rendered directly rather than through a preparer's {@code indexTypeMapper()}, because that
 * method also performs the HTTP PUT. The model still comes from the real {@link AbstractIndexPreparer}, so the
 * wiring between configuration and template stays under test.
 */
class IndexTemplateTest {

    /**
     * Every template that can render lifecycle settings. event-metrics is excluded: it hardcodes its policy
     * and takes no configuration, which is tracked separately.
     */
    static Stream<Arguments> trees_and_lifecycle_templates() {
        return Stream.of("es7x", "es8x", "es9x", "opensearch").flatMap(esDir ->
            Stream.of("request", "health", "monitor", "log", "v4-log", "v4-metrics", "v4-message-log", "v4-message-metrics").map(template ->
                Arguments.of(esDir, template)
            )
        );
    }

    @ParameterizedTest(name = "{0} {1} template uses the configured ISM property names")
    @MethodSource("trees_and_lifecycle_templates")
    void should_render_configured_ism_property_names_instead_of_hardcoded_ilm_keys(String esDir, String template) {
        var configuration = configurationWithPolicies();
        configuration.setIndexLifecyclePolicyPropertyName("index.plugins.index_state_management.policy_id");
        configuration.setIndexLifecycleRolloverAliasPropertyName("index.plugins.index_state_management.rollover_alias");

        assertThat(render(esDir, template, configuration))
            .contains("\"index.plugins.index_state_management.policy_id\"")
            .contains("\"index.plugins.index_state_management.rollover_alias\"")
            .doesNotContain("\"index.lifecycle.");
    }

    @ParameterizedTest(name = "{0} {1} template falls back to the default keys when property names are blank")
    @MethodSource("trees_and_lifecycle_templates")
    void should_fall_back_to_default_property_names_when_configured_blank(String esDir, String template) {
        var configuration = configurationWithPolicies();
        configuration.setIndexLifecyclePolicyPropertyName("");
        configuration.setIndexLifecycleRolloverAliasPropertyName("  index.lifecycle.rollover_alias  ");

        // An empty or padded key would make the cluster reject the whole template, taking the shard, replica
        // and refresh settings down with it — blank has to mean "unset", not "render nothing at all".
        assertThat(render(esDir, template, configuration))
            .contains("\"index.lifecycle.name\"")
            .contains("\"index.lifecycle.rollover_alias\"")
            .doesNotContain("\"\":")
            .doesNotContain("\"  index.lifecycle.rollover_alias  \"");
    }

    @Test
    void should_render_default_ilm_keys_for_an_untouched_elasticsearch_configuration() {
        assertThat(render("es8x", "log", configurationWithPolicies()))
            .contains("\"index.lifecycle.name\": \"policy-log\"")
            .contains("\"index.lifecycle.rollover_alias\"");
    }

    @Test
    void should_escape_property_names_and_policies_so_a_malformed_one_cannot_break_the_json_body() {
        var configuration = configurationWithPolicies();
        configuration.setIndexLifecyclePolicyPropertyName("bad\"name");
        configuration.setIndexLifecycleRolloverAliasPropertyName("bad\"alias");
        configuration.setIndexLifecyclePolicyLog("bad\"policy");

        assertThat(render("es8x", "log", configuration))
            .contains("\"bad\\\"name\"")
            .contains("\"bad\\\"alias\"")
            .contains("\"bad\\\"policy\"");
    }

    private static ReporterConfiguration configurationWithPolicies() {
        var configuration = new ReporterConfiguration();
        // @Value defaults only apply under Spring and these fields have no initialisers, so a
        // hand-built configuration must supply anything the templates interpolate unguarded.
        configuration.setIndexName("gravitee");
        configuration.setRefreshInterval("5s");
        configuration.setNumberOfShards(1);
        configuration.setNumberOfReplicas(1);
        configuration.setIndexLifecyclePolicyHealth("policy-health");
        configuration.setIndexLifecyclePolicyMonitor("policy-monitor");
        configuration.setIndexLifecyclePolicyRequest("policy-request");
        configuration.setIndexLifecyclePolicyLog("policy-log");
        return configuration;
    }

    private static String render(String esDir, String template, ReporterConfiguration configuration) {
        var freeMarkerComponent = FreeMarkerComponent.builder()
            .classLoader(IndexTemplateTest.class.getClassLoader())
            .classLoaderTemplateBase("freemarker")
            .build();
        var pipelineConfiguration = new PipelineConfiguration(null, null, freeMarkerComponent);

        // getTemplateData() is shared by every preparer and carries no per-distribution state, so any one of
        // them produces the model for all four template trees. No client: nothing here talks to a cluster.
        var preparer = new ES8IndexPreparer(configuration, pipelineConfiguration, freeMarkerComponent, null);
        var data = preparer.getTemplateData();
        data.put("indexName", configuration.getIndexName() + '-' + template);

        return freeMarkerComponent.generateFromTemplate("/" + esDir + "/mapping/index-template-" + template + ".ftl", data);
    }
}
