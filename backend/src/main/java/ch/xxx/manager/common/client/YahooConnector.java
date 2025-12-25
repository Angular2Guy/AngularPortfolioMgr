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
package ch.xxx.manager.common.client;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.xxx.manager.stocks.dto.YahooDailyQuoteImportDto;
import ch.xxx.manager.stocks.mapping.open.YahooClientMapper;
import ch.xxx.manager.stocks.YahooClient;

@Component
public class YahooConnector implements YahooClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(YahooConnector.class);
	private final ConnectorClient connectorClient;
	private final YahooClientMapper yahooDtoMapper;

	public YahooConnector(YahooClientMapper yahooDtoMapper, ConnectorClient connectorClient) {
		this.connectorClient = connectorClient;
		this.yahooDtoMapper = yahooDtoMapper;
	}

	@Override
	public List<YahooDailyQuoteImportDto> getTimeseriesDailyHistory(String symbol, Duration delay) {
		LocalDateTime toTime = LocalDateTime.now();
		LocalDateTime fromTime = LocalDateTime.of(2000, 1, 1, 0, 0);
		// https://query1.finance.yahoo.com/v8/finance/chart/IBM?events=capitalGain|div|split&formatted=true&includeAdjustedClose=true&interval=1d&period1=1378684800&period2=1726501463&symbol=IBM&userYfid=true&lang=en-US&region=US
		final String myUrl = String.format(
				"https://query1.finance.yahoo.com/v8/finance/chart/%s?events=capitalGain|div|split&formatted=true&includeAdjustedClose=true&interval=1d&period1=%d&period2=%d&symbol=%s&userYfid=true&lang=en-US&region=US",
				// "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%d&period2=%d&interval=1d&events=history",
				symbol, fromTime.toEpochSecond(OffsetDateTime.now().getOffset()),
				toTime.toEpochSecond(OffsetDateTime.now().getOffset()), symbol);
		LOGGER.info(myUrl);
		return this.connectorClient.restCall(myUrl, String.class).stream().map(this.yahooDtoMapper::convert)
				.flatMap(YahooClientMapper::convert).toList();
	}
}
