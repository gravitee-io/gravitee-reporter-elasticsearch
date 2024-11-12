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
package io.gravitee.reporter.elasticsearch;

import io.gravitee.elasticsearch.config.Endpoint;
import io.gravitee.reporter.elasticsearch.config.ReporterConfiguration;
import java.time.Duration;
import java.util.Collections;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Spring configuration used for testing purpose.
 */
@Configuration
@Import(UnitTestConfiguration.class)
public class OpensearchIntegrationTestConfiguration {

    private static final String OPENSEARCH_DEFAULT_VERSION = "2.11.0";
    public static final String CLUSTER_NAME = "gravitee_test";

    @Value("${opensearch.version:" + OPENSEARCH_DEFAULT_VERSION + "}")
    private String opensearchVersion;

    @Bean
    public ReporterConfiguration configuration(OpensearchContainer<?> opensearchContainer) {
        ReporterConfiguration configuration = new ReporterConfiguration();
        configuration.setEndpoints(
            Collections.singletonList(
                new Endpoint("http://" + opensearchContainer.getHost() + ":" + opensearchContainer.getMappedPort(9200))
            )
        );
        configuration.setIndexMode("ism");
        return configuration;
    }

    @Bean(destroyMethod = "close")
    public OpensearchContainer<?> opensearchContainer() {
        final OpensearchContainer<?> opensearchContainer = new OpensearchContainer<>("opensearchproject/opensearch:" + opensearchVersion);
        opensearchContainer
            .withEnv("cluster.name", CLUSTER_NAME)
            .withEnv("DISABLE_SECURITY_PLUGIN", "true")
            .withEnv("DISABLE_INSTALL_DEMO_CONFIG", "true")
            .withExposedPorts(9200);

        opensearchContainer.start();
        return opensearchContainer;
    }
}
