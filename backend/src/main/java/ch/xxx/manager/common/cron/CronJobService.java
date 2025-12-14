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
package ch.xxx.manager.common.cron;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ch.xxx.manager.stocks.entity.Symbol;
import ch.xxx.manager.common.utils.DataHelper;
import ch.xxx.manager.common.AppUserService;
import ch.xxx.manager.stocks.ComparisonIndex;
import ch.xxx.manager.stocks.CurrencyService;
import ch.xxx.manager.findata.NewsFeedService;
import ch.xxx.manager.stocks.PortfolioService;
import ch.xxx.manager.stocks.QuoteImportService;
import ch.xxx.manager.stocks.QuoteImportService.UserKeys;
import ch.xxx.manager.stocks.SymbolImportService;
import jakarta.transaction.Transactional;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Service
public class CronJobService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CronJobService.class);	
	private final SymbolImportService symbolImportService;
	private final QuoteImportService quoteImportService;
	private final CurrencyService currencyService;
	private final AppUserService appUserService;
	private final PortfolioService portfolioService;
	private final NewsFeedService newsFeedService;
	private final Environment environment;
	@Value("${api.key}")
	private String apiKey;

	public CronJobService(SymbolImportService symbolImportService, PortfolioService portfolioService, 
			QuoteImportService quoteImportService, CurrencyService currencyService, AppUserService appUserService,
			Environment environment, NewsFeedService newsFeedService) {
		this.symbolImportService = symbolImportService;
		this.quoteImportService = quoteImportService;
		this.currencyService = currencyService;
		this.appUserService = appUserService;
		this.portfolioService = portfolioService;
		this.environment = environment;
		this.newsFeedService = newsFeedService;
	}

	@Scheduled(fixedRate = 90000)
	@Order(1)
	public void updateLoggedOutUsers() {
		if (Stream.of(this.environment.getActiveProfiles()).noneMatch(myProfile -> myProfile.contains("kafka"))) {
			LOGGER.info("Update logged out users.");
			this.appUserService.updateLoggedOutUsers();
		}
	}

	@Scheduled(cron = "0 1 * * * ?")
	@Order(2)
	public void updateNewsFeeds() {
		this.newsFeedService.updateCnbcFinanceNewsFeed();
		this.newsFeedService.updateSeekingAlphaNewsFeed();
	}
	
	@Scheduled(cron = "5 0 1 * * ?")
	@SchedulerLock(name = "CleanUp_scheduledTask", lockAtLeastFor = "PT2H", lockAtMostFor = "PT3H")
	public void dbCleanup() {
		LOGGER.info("Start cleanup Job");
		this.appUserService.cleanup();
		LOGGER.info("End cleanup Job");
	}
	
	@Scheduled(cron = "10 0 * * * ?")
	@SchedulerLock(name = "EventRetry_scheduledTask", lockAtLeastFor = "PT45M", lockAtMostFor = "PT55M")
	public void eventRetry() {
		LOGGER.info("Start eventretry Job");
		this.appUserService.eventRetry();
		LOGGER.info("End eventretry Job");
	}
	
	@Transactional
	@Scheduled(cron = "0 0 1 * * ?")
	@SchedulerLock(name = "CronJob_symbols", lockAtLeastFor = "PT10M", lockAtMostFor = "PT2H")
	public void scheduledImporterSymbols() {
		Instant start = Instant.now();
		importCurrencyQuotes();
		LOGGER.info(this.symbolImportService.importDeSymbols());
		LOGGER.info(this.symbolImportService.importHkSymbols());
		LOGGER.info(this.symbolImportService.importUsSymbols());
		this.symbolImportService.refreshSymbolEntities();
		LOGGER.info("scheduledImporterSymbols: {}ms", Instant.now().toEpochMilli() - start.toEpochMilli());
	}

	private void importCurrencyQuotes() {
		Instant start = Instant.now();
		LOGGER.info("Import of {} Hkd quotes finished.",
				this.currencyService.importFxDailyQuoteHistory(DataHelper.CurrencyKey.HKD.toString()));
		LOGGER.info("Import of {} Usd quotes finished.",
				this.currencyService.importFxDailyQuoteHistory(DataHelper.CurrencyKey.USD.toString()));
		this.currencyService.initCurrencyMap();
		LOGGER.info("importCurrencyQuotes: {}ms", Instant.now().toEpochMilli() - start.toEpochMilli());
	}

	@Transactional
	@Scheduled(cron = "0 10 1 * * ?")
	@SchedulerLock(name = "CronJob_refIndexes", lockAtLeastFor = "PT10M", lockAtMostFor = "PT2H")
	public void scheduledImporterRefIndexes() {
		Instant start = Instant.now();
		List<String> symbols = this.symbolImportService
				.importReferenceIndexes(List.of(ComparisonIndex.SP500.getSymbol(),
						ComparisonIndex.EUROSTOXX50.getSymbol(), ComparisonIndex.MSCI_CHINA.getSymbol()));
		Long symbolCount = symbols.stream().map(
				mySymbol -> this.quoteImportService.importUpdateDailyQuotes(mySymbol, new UserKeys(this.apiKey, null)))
				.reduce(0L, (acc, value) -> acc + value);
		LOGGER.info("Indexquotes import done for: {}", symbolCount);
		LOGGER.info("scheduledImporterRefIndexes: {}ms", Instant.now().toEpochMilli() - start.toEpochMilli());
	}

	@Transactional
	@Scheduled(cron = "0 20 1 * * ?")	
	@SchedulerLock(name = "CronJob_quotes", lockAtLeastFor = "PT10M", lockAtMostFor = "PT2H")
	public void scheduledImporterQuotes() {
		Instant start = Instant.now();
		List<Symbol> symbolsToUpdate = this.symbolImportService.findSymbolsToUpdate();
		//LOGGER.info("Symbols to update: {}", symbolsToUpdate.size());
		try {
			var numSym = this.symbolImportService.updateSymbolQuotes(symbolsToUpdate).get(110L, TimeUnit.MINUTES);
			LOGGER.info("Symbols updated: {}", numSym);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException("SymbolImport failed.",e);
		}
		LOGGER.info("scheduledImporterQuotes: {}ms", Instant.now().toEpochMilli() - start.toEpochMilli());
	}
	
	@Transactional
	@Scheduled(cron = "0 40 1 * * ?")
	@SchedulerLock(name = "CronJob_portfolios", lockAtLeastFor = "PT10M", lockAtMostFor = "PT2H")
	public void scheduledUpdatePortfolios() {
		Instant start = Instant.now();
		this.portfolioService.findAllPortfolios().forEach(myPortfolio -> this.portfolioService.updatePortfolioValues(myPortfolio));
		LOGGER.info("scheduledUpdatePortfolios: {}ms", Instant.now().toEpochMilli() - start.toEpochMilli());
	}
}
