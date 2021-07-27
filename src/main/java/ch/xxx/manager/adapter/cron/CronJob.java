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
package ch.xxx.manager.adapter.cron;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.utils.CurrencyKey;
import ch.xxx.manager.usecase.service.AlphavatageClient;
import ch.xxx.manager.usecase.service.ComparisonIndex;
import ch.xxx.manager.usecase.service.PortfolioCalculationService;
import ch.xxx.manager.usecase.service.QuoteImportService;
import ch.xxx.manager.usecase.service.SymbolImportService;
import ch.xxx.manager.usecase.service.YahooClient;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
public class CronJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(CronJob.class);
	private final YahooClient yahooClient;
	private final AlphavatageClient alphavatageClient;
	private final SymbolImportService symbolImportService;
	private final QuoteImportService quoteImportService;
	private final PortfolioCalculationService portfolioCalculationService;

	public CronJob(YahooClient yahooClient,
			AlphavatageClient alphavatageClient, SymbolImportService symbolImportService,
			QuoteImportService quoteImportService, PortfolioCalculationService portfolioCalculationService) {
		this.yahooClient = yahooClient;
		this.alphavatageClient = alphavatageClient;
		this.symbolImportService = symbolImportService;
		this.portfolioCalculationService = portfolioCalculationService;
		this.quoteImportService = quoteImportService;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("init called");
	}

	@Scheduled(cron = "0 0 1 * * ?")
	@SchedulerLock(name = "CronJob_symbols", lockAtLeastFor = "PT10M", lockAtMostFor = "PT2H")
	public void scheduledImporterSymbols() {
		LOGGER.info("Import of {} Hkd quotes finished.",
				this.quoteImportService.importFxDailyQuoteHistory(CurrencyKey.HKD.toString()));
		LOGGER.info("Import of {} Usd quotes finished.",
				this.quoteImportService.importFxDailyQuoteHistory(CurrencyKey.USD.toString()));
		LOGGER.info(this.symbolImportService.importDeSymbols());
		LOGGER.info(this.symbolImportService.importHkSymbols());
		LOGGER.info(this.symbolImportService.importUsSymbols());
		this.symbolImportService.refreshSymbolEntities();
	}

	@Scheduled(cron = "0 0 10 * * ?")
	@SchedulerLock(name = "CronJob_refIndexes", lockAtLeastFor = "PT10M", lockAtMostFor = "PT2H")
	public void scheduledImporterRefIndexes() {
		List<String> symbols = this.symbolImportService.importReferenceIndexes(List.of(ComparisonIndex.SP500.getSymbol(),
				ComparisonIndex.EUROSTOXX50.getSymbol(), ComparisonIndex.MSCI_CHINA.getSymbol()));
		Long symbolCount = symbols.stream().map(mySymbol -> this.quoteImportService.importUpdateDailyQuotes(mySymbol))
				.reduce(0L, (acc, value) -> acc + value);
		LOGGER.info("Indexquotes import done for: {}", symbolCount);
	}
}
