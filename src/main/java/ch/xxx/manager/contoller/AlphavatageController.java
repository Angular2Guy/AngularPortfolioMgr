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
package ch.xxx.manager.contoller;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import ch.xxx.manager.dto.DailyWrapperImportDto;
import reactor.core.publisher.Mono;

@Component
public class AlphavatageController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlphavatageController.class);
	@Value("${api.key}")
	private String apiKey;
	
	public void getQuote(String symbol) {
		
	}
	
	public void getTimeseriesToday(String symbol) {
		
	}
	
	public Mono<DailyWrapperImportDto> getTimeseriesHistory(String symbol) {
		try {
			return WebClient.create().get()
				.uri(new URI(String.format("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=%s&outputsize=full&apikey=%s", symbol, this.apiKey)))
				.retrieve().bodyToMono(DailyWrapperImportDto.class);
		} catch (URISyntaxException e) {
			LOGGER.error("getTimeseriesHistory failed.",e);
		}
		return Mono.empty();
	}
}
