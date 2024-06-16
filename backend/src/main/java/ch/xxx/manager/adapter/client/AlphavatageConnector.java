/**
     Copyright 2019 Sven Loesekann
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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.AlphaOverviewImportDto;
import ch.xxx.manager.domain.model.dto.DailyFxWrapperImportDto;
import ch.xxx.manager.domain.model.dto.DailyWrapperImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayWrapperImportDto;
import ch.xxx.manager.domain.utils.DataHelper.CurrencyKey;
import ch.xxx.manager.usecase.service.AlphavatageClient;
import ch.xxx.manager.usecase.service.QuoteImportService.UserKeys;
import jakarta.annotation.PostConstruct;

@Component
public class AlphavatageConnector implements AlphavatageClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlphavatageConnector.class);
	@Value("${api.key}")
	private String apiKey;
	@Value("${show.api.key}")
	private String showApiKey;

	@PostConstruct
	public void init() {
		if ("true".equalsIgnoreCase(this.showApiKey)) {
			LOGGER.info("ApiKey: " + apiKey);
		}
	}

	@Override
	public Optional<AlphaOverviewImportDto> importCompanyProfile(String symbol) {
		final String myUrl = String.format("https://www.alphavantage.co/query?function=OVERVIEW&symbol=%s&apikey=%s",
				symbol, this.apiKey);
		LOGGER.info(myUrl);
		return ConnectorUtils.restCall(myUrl, AlphaOverviewImportDto.class);
	}

	@Override
	public Optional<IntraDayWrapperImportDto> getTimeseriesIntraDay(String symbol, UserKeys userKeys) {
			final String myUrl = String.format(
					"https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=5min&outputsize=full&apikey=%s",
					symbol, userKeys.alphavantageKey());
			LOGGER.info(myUrl);
			return ConnectorUtils.restCall(myUrl, IntraDayWrapperImportDto.class);
	}

	@Override
	public Optional<DailyWrapperImportDto> getTimeseriesDailyHistory(String symbol, boolean fullSeries, UserKeys userKeys) {
			final String fullSeriesStr = fullSeries ? "&outputsize=full" : "";
			final String urlBase = false
					? "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=%s%s&apikey=%s"
					: "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s%s&apikey=%s";
			final String myUrl = String.format(urlBase, symbol, fullSeriesStr, userKeys.alphavantageKey());
			LOGGER.info(myUrl);
			return ConnectorUtils.restCall(myUrl, DailyWrapperImportDto.class);
	}

	@Override
	public Optional<DailyFxWrapperImportDto> getFxTimeseriesDailyHistory(String to_currency, boolean fullSeries) {
			final String from_currency = CurrencyKey.EUR.toString();
			String fullSeriesStr = fullSeries ? "&outputsize=full" : "";
			final String myUrl = String.format(
					"https://www.alphavantage.co/query?function=FX_DAILY&from_symbol=%s&to_symbol=%s%s&apikey=%s",
					from_currency, to_currency, fullSeriesStr, this.apiKey);
			LOGGER.info(myUrl);
			return ConnectorUtils.restCall(myUrl, DailyFxWrapperImportDto.class);
	}
}
