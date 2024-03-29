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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import ch.xxx.manager.domain.model.dto.HkDailyQuoteImportDto;
import ch.xxx.manager.usecase.service.YahooClient;
import reactor.core.publisher.Mono;

@Component
public class YahooConnector implements YahooClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(YahooConnector.class);
	private final CsvMapper csvMapper;

	public YahooConnector(@Qualifier("csv") CsvMapper csvMapper) {
		this.csvMapper = csvMapper;
	}
	
//	@jakarta.annotation.PostConstruct
//	public void init() {
//		this.csvMapper.registerModule(new JavaTimeModule());
//	}

	public Mono<List<HkDailyQuoteImportDto>> getTimeseriesDailyHistory(String symbol) {
		try {
			LocalDateTime toTime = LocalDateTime.now();
			LocalDateTime fromTime = toTime.minusYears(10);
			final String myUrl = String.format(
					"https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%d&period2=%d&interval=1d&events=history",
					symbol, fromTime.toEpochSecond(OffsetDateTime.now().getOffset()),
					toTime.toEpochSecond(OffsetDateTime.now().getOffset()));
			LOGGER.info(myUrl);
			return WebClient.create().mutate().exchangeStrategies(ConnectorUtils.createLargeResponseStrategy()).build()
					.get().uri(new URI(myUrl)).retrieve().toEntity(String.class)
					.flatMap(response -> this.convert(response.getBody()));
		} catch (URISyntaxException e) {
			LOGGER.error("getTimeseriesHistory failed.", e);
		}
		return Mono.empty();
	}

	private Mono<List<HkDailyQuoteImportDto>> convert(String linesStr) {
		try {
			MappingIterator<HkDailyQuoteImportDto> mappingIterator = csvMapper.readerFor(HkDailyQuoteImportDto.class)
					.with(CsvSchema.emptySchema().withHeader()).readValues(linesStr);
			return Mono.just(mappingIterator.readAll());
		} catch (IOException e) {
			LOGGER.error("Csv import failed.", e);
		}
		return Mono.just(List.of());
	}
}
