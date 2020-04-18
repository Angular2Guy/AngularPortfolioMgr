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
package ch.xxx.manager.connector;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import ch.xxx.manager.dto.HkSymbolImportDto;
import reactor.core.publisher.Flux;

@Component
public class HkexConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(HkexConnector.class);
	
	public Flux<HkSymbolImportDto> importSymbols() {
		try {
			return WebClient.create().mutate().exchangeStrategies(ConnectorUtils.createLargeResponseStrategy()).build().get()
				.uri(new URI("https://www.hkexnews.hk/ncms/script/eds/activestock_sehk_e.json"))
				.retrieve().bodyToFlux(HkSymbolImportDto.class);
		} catch (URISyntaxException e) {
			LOGGER.error("Import hk symbols failed.",e);
		}
		return Flux.empty();
	}
}
