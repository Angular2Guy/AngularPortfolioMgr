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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import ch.xxx.manager.dto.HkDailyQuoteImportDto;
import reactor.core.publisher.Mono;

@Component
public class YahooConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(YahooConnector.class);

	public Mono<HkDailyQuoteImportDto> getTimeseriesDailyHistory(String symbol) {
		try {
			LocalDateTime toTime = LocalDateTime.now();
			LocalDateTime fromTime = toTime.minusYears(10);
			return WebClient.create().mutate().exchangeStrategies(ConnectorUtils.createLargeResponseStrategy()).build()
					.get()
					.uri(new URI(String.format(
							"https://query1.finance.yahoo.com/v7/finance/download/%s.HK?period1=%d&period2=%d&interval=1d&events=history",
							symbol, fromTime.toEpochSecond(OffsetDateTime.now().getOffset()),
							toTime.toEpochSecond(OffsetDateTime.now().getOffset()))))
					.retrieve().bodyToMono(HkDailyQuoteImportDto.class);
		} catch (URISyntaxException e) {
			LOGGER.error("getTimeseriesHistory failed.", e);
		}
		return Mono.empty();
	}
}
