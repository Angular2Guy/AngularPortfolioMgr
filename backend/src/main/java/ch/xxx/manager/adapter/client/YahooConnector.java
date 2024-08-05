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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import ch.xxx.manager.domain.model.dto.YahooDailyQuoteImportDto;
import ch.xxx.manager.usecase.service.YahooClient;

@Component
public class YahooConnector implements YahooClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(YahooConnector.class);
	private final CsvMapper csvMapper;
	private final ConnectorClient connectorClient;

	public YahooConnector(@Qualifier("csv") CsvMapper csvMapper, ConnectorClient connectorClient) {
		this.csvMapper = csvMapper;
		this.connectorClient = connectorClient;
	}

	@Override
	public List<YahooDailyQuoteImportDto> getTimeseriesDailyHistory(String symbol) {
		LocalDateTime toTime = LocalDateTime.now();
		LocalDateTime fromTime = toTime.minusYears(10);
		final String myUrl = String.format(
				"https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%d&period2=%d&interval=1d&events=history",
				symbol, fromTime.toEpochSecond(OffsetDateTime.now().getOffset()),
				toTime.toEpochSecond(OffsetDateTime.now().getOffset()));
		LOGGER.info(myUrl);
		return this.connectorClient.restCall(myUrl, String.class).stream()
				.flatMap(response -> this.convert(response).stream()).toList();
	}

	private List<YahooDailyQuoteImportDto> convert(String linesStr) {
		try {
			MappingIterator<YahooDailyQuoteImportDto> mappingIterator = csvMapper.readerFor(YahooDailyQuoteImportDto.class)
					.with(CsvSchema.emptySchema().withHeader()).readValues(linesStr);
			return mappingIterator.readAll();
		} catch (IOException e) {
			throw new RuntimeException("Csv import failed.", e);
		}
	}
}
