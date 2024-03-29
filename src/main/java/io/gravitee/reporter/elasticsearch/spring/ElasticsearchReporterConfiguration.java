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
package io.gravitee.reporter.elasticsearch.spring;

import io.gravitee.common.templating.FreeMarkerComponent;
import io.gravitee.elasticsearch.client.Client;
import io.gravitee.elasticsearch.client.http.*;
import io.gravitee.reporter.elasticsearch.config.PipelineConfiguration;
import io.gravitee.reporter.elasticsearch.config.ReporterConfiguration;
import io.vertx.rxjava3.core.Vertx;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchReporterConfiguration {

    @Bean
    public Vertx vertxRx(io.vertx.core.Vertx vertx) {
        return Vertx.newInstance(vertx);
    }

    @Bean
    public Client httpClient(ReporterConfiguration reporterConfiguration) {
        HttpClientConfiguration clientConfiguration = new HttpClientConfiguration();
        clientConfiguration.setEndpoints(reporterConfiguration.getEndpoints());
        clientConfiguration.setUsername(reporterConfiguration.getUsername());
        clientConfiguration.setPassword(reporterConfiguration.getPassword());
        clientConfiguration.setRequestTimeout(reporterConfiguration.getRequestTimeout());

        clientConfiguration.setProxyType(reporterConfiguration.getProxyType());
        clientConfiguration.setProxyHttpHost(reporterConfiguration.getProxyHttpHost());
        clientConfiguration.setProxyHttpPort(reporterConfiguration.getProxyHttpPort());
        clientConfiguration.setProxyHttpUsername(reporterConfiguration.getProxyHttpUsername());
        clientConfiguration.setProxyHttpPassword(reporterConfiguration.getProxyHttpPassword());
        clientConfiguration.setProxyHttpsHost(reporterConfiguration.getProxyHttpsHost());
        clientConfiguration.setProxyHttpsPort(reporterConfiguration.getProxyHttpsPort());
        clientConfiguration.setProxyHttpsUsername(reporterConfiguration.getProxyHttpsUsername());
        clientConfiguration.setProxyHttpsPassword(reporterConfiguration.getProxyHttpsPassword());
        clientConfiguration.setProxyConfigured(reporterConfiguration.isProxyConfigured());

        if (reporterConfiguration.getSslKeystoreType() != null) {
            if (reporterConfiguration.getSslKeystoreType().equalsIgnoreCase(ClientSslConfiguration.JKS_KEYSTORE_TYPE)) {
                clientConfiguration.setSslConfig(
                    new HttpClientJksSslConfiguration(
                        reporterConfiguration.getSslKeystore(),
                        reporterConfiguration.getSslKeystorePassword()
                    )
                );
            } else if (reporterConfiguration.getSslKeystoreType().equalsIgnoreCase(ClientSslConfiguration.PFX_KEYSTORE_TYPE)) {
                clientConfiguration.setSslConfig(
                    new HttpClientPfxSslConfiguration(
                        reporterConfiguration.getSslKeystore(),
                        reporterConfiguration.getSslKeystorePassword()
                    )
                );
            } else if (reporterConfiguration.getSslKeystoreType().equalsIgnoreCase(ClientSslConfiguration.PEM_KEYSTORE_TYPE)) {
                clientConfiguration.setSslConfig(
                    new HttpClientPemSslConfiguration(reporterConfiguration.getSslPemCerts(), reporterConfiguration.getSslPemKeys())
                );
            }
        }

        return new HttpClient(clientConfiguration);
    }

    @Bean
    public ReporterConfiguration configuration() {
        return new ReporterConfiguration();
    }

    @Bean
    public FreeMarkerComponent freeMarkerComponent(
        @Value("${reporters.elasticsearch.template_mapping.path:#{null}}") String templateMappingPath
    ) {
        return FreeMarkerComponent
            .builder()
            .path(templateMappingPath != null ? Path.of(templateMappingPath) : null)
            .classLoader(getClass().getClassLoader())
            .classLoaderTemplateBase("freemarker")
            .build();
    }

    @Bean
    public PipelineConfiguration pipelineConfiguration(
        @Value("${reporters.elasticsearch.pipeline.plugins.ingest:geoip,user_agent}") String ingestPlugins,
        @Value("${reporters.elasticsearch.user_agent.regex_file:#{null}}") String userAgentRegexFile,
        FreeMarkerComponent freeMarkerComponent
    ) {
        return new PipelineConfiguration(ingestPlugins, userAgentRegexFile, freeMarkerComponent);
    }
}
