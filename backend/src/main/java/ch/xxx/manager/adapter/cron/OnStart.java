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
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.FeConceptDto;
import ch.xxx.manager.domain.model.dto.SfCountryDto;
import ch.xxx.manager.domain.model.dto.SfQuarterDto;
import ch.xxx.manager.usecase.service.FinancialDataService;
import ch.xxx.manager.usecase.service.NewsFeedService;
import ch.xxx.manager.usecase.service.PortfolioService;
import ch.xxx.manager.usecase.service.SymbolImportService;
import jakarta.annotation.PostConstruct;

@Component
public class OnStart {
	private static final Logger LOGGER = LoggerFactory.getLogger(OnStart.class);
	private final SymbolImportService symbolImportService;
	private final FinancialDataService financialDataService;
	private final PortfolioService portfolioService;
	private final NewsFeedService newsFeedService;

	public OnStart(SymbolImportService symbolImportService, FinancialDataService financialDataImportService, PortfolioService portfolioService, NewsFeedService newsFeedService) {
		this.symbolImportService = symbolImportService;
		this.financialDataService = financialDataImportService;
		this.portfolioService = portfolioService;
		this.newsFeedService = newsFeedService;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("init called");
	}

	@Async
	@EventListener(ApplicationReadyEvent.class)
	public void startupDone() throws InterruptedException, ExecutionException {
		this.newsFeedService.updateCnbcFinanceNewsFeed();		
		this.newsFeedService.updateSeekingAlphaNewsFeed();
		this.newsFeedService.importCompanyReports();
		this.symbolImportService.refreshSymbolEntities();
		LOGGER.info("Symbols refreshed");
		List<FeConceptDto> feConcepts = this.financialDataService.findFeConcepts();
		LOGGER.info("Concept count {}", feConcepts.size());
		List<SfQuarterDto> sfQuarters = this.financialDataService.findSfQuarters();
		LOGGER.info("Quarter count {}", sfQuarters.size());
		List<SfCountryDto> sfCountries = this.financialDataService.findSfCountries();
		LOGGER.info("Country count {}", sfCountries.size());
		var portfolioList = this.portfolioService.findAllPortfolios();
		portfolioList.forEach(myPortfolio -> this.portfolioService.updatePortfolioValues(myPortfolio));
		LOGGER.info("Portfolios updated {}", portfolioList.size());
	}
}
