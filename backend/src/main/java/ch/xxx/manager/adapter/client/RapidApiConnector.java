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
package ch.xxx.manager.adapter.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import ch.xxx.manager.domain.model.dto.RapidOverviewImportDto;
import ch.xxx.manager.usecase.service.RapidApiClient;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Component
public class RapidApiConnector implements RapidApiClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RapidApiConnector.class);
	@Value("${api.key.rapidapi}")
	private String apiKey;
	@Value("${show.api.key}")
	private String showApiKey;

	@PostConstruct
	public void init() {
		if ("true".equalsIgnoreCase(this.showApiKey)) {
			LOGGER.info("RapidApiKey: " + apiKey);
		}
	}

	@Override
	public Mono<RapidOverviewImportDto> importCompanyProfile(String symbol) {
		try {
			final String myUrl = String.format("https://yh-finance.p.rapidapi.com/stock/v2/get-profile?symbol=%s", symbol);
			LOGGER.info(myUrl);
			return WebClient.create().mutate().exchangeStrategies(ConnectorUtils.createLargeResponseStrategy()).build()
					.get().uri(new URI(myUrl))
					.header("X-RapidAPI-Key", this.apiKey)
					.header("X-RapidAPI-Host", "yh-finance.p.rapidapi.com")
					.retrieve().bodyToMono(RapidOverviewImportDto.class);
		} catch (URISyntaxException e) {
			LOGGER.info("importCompanyProfile failed.", e);
		}
		return Mono.empty();
	}
}
