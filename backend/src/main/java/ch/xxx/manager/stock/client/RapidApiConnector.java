/**
 *    Copyright 2019 Sven Loesekann
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ch.xxx.manager.stock.client;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import ch.xxx.manager.common.client.ConnectorClient;
import ch.xxx.manager.domain.model.dto.RapidOverviewImportDto;
import ch.xxx.manager.usecase.service.RapidApiClient;
import jakarta.annotation.PostConstruct;

@Component
public class RapidApiConnector implements RapidApiClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RapidApiConnector.class);
	@Value("${api.key.rapidapi}")
	private String apiKey;
	@Value("${show.api.key}")
	private String showApiKey;
	private final ConnectorClient connectorClient;
	
	public RapidApiConnector(ConnectorClient connectorClient) {
		this.connectorClient = connectorClient;
	}

	@PostConstruct
	public void init() {
		if ("true".equalsIgnoreCase(this.showApiKey)) {
			LOGGER.info("RapidApiKey: " + apiKey);
		}
	}
	
	@Override
	public Optional<RapidOverviewImportDto> importCompanyProfile(String symbol, Duration delay) {
			final String myUrl = String.format("https://yh-finance.p.rapidapi.com/stock/v2/get-profile?symbol=%s", symbol);
			LOGGER.info(myUrl);
			var headerMultiMap = new LinkedMultiValueMap<String, String>();
			headerMultiMap.put("X-RapidAPI-Key", List.of(this.apiKey));
			headerMultiMap.put("X-RapidAPI-Host", List.of("yh-finance.p.rapidapi.com"));
			return this.connectorClient.restCall(myUrl, headerMultiMap, RapidOverviewImportDto.class, delay);					
	}
}
