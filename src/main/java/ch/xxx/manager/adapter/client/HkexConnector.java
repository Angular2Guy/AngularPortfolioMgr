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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import ch.xxx.manager.domain.model.dto.HkSymbolImportDto;
import ch.xxx.manager.usecase.service.HkexClient;
import reactor.core.publisher.Mono;

@Component
public class HkexConnector implements HkexClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(HkexConnector.class);
	
	public Mono<List<HkSymbolImportDto>> importSymbols() {
		try {
			return WebClient.create().mutate().exchangeStrategies(ConnectorUtils.createLargeResponseStrategy()).build().get()
				.uri(new URI("https://www.hkexnews.hk/ncms/script/eds/activestock_sehk_e.json"))
				.retrieve().bodyToFlux(HkSymbolImportDto.class).collectList();
		} catch (URISyntaxException e) {
			LOGGER.error("Import hk symbols failed.",e);
		}
		return Mono.empty();
	}
}
