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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.xxx.manager.dto.HkDailyQuoteImportDto;
import reactor.core.publisher.Mono;

@Component
public class YahooConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(YahooConnector.class);
	private CsvMapper csvMapper = new CsvMapper();
	
	@PostConstruct
	public void init() {
		this.csvMapper.registerModule(new JavaTimeModule());
	}

	public Mono<List<HkDailyQuoteImportDto>> getTimeseriesDailyHistory(String symbol) {
		try {
			LocalDateTime toTime = LocalDateTime.now();
			LocalDateTime fromTime = toTime.minusYears(10);
			return WebClient.create().mutate().exchangeStrategies(ConnectorUtils.createLargeResponseStrategy()).build()
					.get()
					.uri(new URI(String.format(
							"https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%d&period2=%d&interval=1d&events=history",
							symbol, fromTime.toEpochSecond(OffsetDateTime.now().getOffset()),
							toTime.toEpochSecond(OffsetDateTime.now().getOffset()))))
					.retrieve().toEntity(String.class).flatMap(response -> this.convert(response.getBody()));
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
