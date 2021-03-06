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
package io.gravitee.reporter.elastic.spring.factory;

import io.gravitee.reporter.elastic.config.ElasticConfiguration;
import io.gravitee.reporter.elastic.model.HostAddress;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class ElasticClientFactory extends AbstractFactoryBean<Client> {

	private final Logger LOGGER = LoggerFactory.getLogger(ElasticClientFactory.class);

	@Autowired
	private ElasticConfiguration configuration;

	@Override
	public Class<Client> getObjectType() {
		return Client.class;
	}
	
	@Override
	protected Client createInstance() throws Exception {
		switch (configuration.getProtocol()) {
			case TRANSPORT:
				return createTransportClient();
			default:
				LOGGER.error("Unsupported protocol [{}] for elastic client", configuration.getProtocol());
				throw new IllegalStateException(String.format("Unsupported protocol [%s] for elastic client", configuration.getProtocol()));
		}
	}

	private Client createTransportClient() {
		Settings settings = Settings.builder()
				.put("cluster.name", configuration.getClusterName()).build();
		TransportClient transportClient = new PreBuiltTransportClient(settings);

		List<HostAddress> adresses = configuration.getHostsAddresses();

		for (HostAddress address : adresses) {
			try {
				transportClient.addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName(address.getHostname()), address.getPort()));
			} catch (UnknownHostException uhe) {
				LOGGER.error("Error while creating transport client for Elastic", uhe);
			}
		}

		return transportClient;
	}
}