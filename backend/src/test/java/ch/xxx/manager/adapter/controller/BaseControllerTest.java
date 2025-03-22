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
package ch.xxx.manager.adapter.controller;

import javax.sql.DataSource;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ch.xxx.manager.adapter.events.EventPublicationsBean;
import ch.xxx.manager.adapter.repository.JpaAppUserRepository;
import ch.xxx.manager.adapter.repository.JpaCurrencyRepository;
import ch.xxx.manager.adapter.repository.JpaDailyQuoteRepository;
import ch.xxx.manager.adapter.repository.JpaFinancialElementRepository;
import ch.xxx.manager.adapter.repository.JpaPortfolioElementRepository;
import ch.xxx.manager.adapter.repository.JpaPortfolioRepository;
import ch.xxx.manager.adapter.repository.JpaPortfolioToSymbolRepository;
import ch.xxx.manager.adapter.repository.JpaRevokedTokenRepository;
import ch.xxx.manager.adapter.repository.JpaSectorRepository;
import ch.xxx.manager.adapter.repository.JpaSymbolFinancialsRepository;
import ch.xxx.manager.adapter.repository.JpaSymbolRepository;
import ch.xxx.manager.usecase.service.AppUserServiceDb;
import ch.xxx.manager.usecase.service.CurrencyService;
import ch.xxx.manager.usecase.service.FinancialDataService;
import ch.xxx.manager.usecase.service.PortfolioToIndexService;
import ch.xxx.manager.usecase.service.QuoteImportService;
import ch.xxx.manager.usecase.service.QuoteService;
import ch.xxx.manager.usecase.service.SymbolImportService;
import ch.xxx.manager.usecase.service.SymbolService;
import jakarta.persistence.EntityManager;

public class BaseControllerTest {
	@MockitoBean	
	protected AppUserServiceDb appUserServiceDb;
	@MockitoBean
	protected SymbolService symbolService;
	@MockitoBean 
	protected FinancialDataService financialDataService;
	@MockitoBean
	protected QuoteService quoteService;
	@MockitoBean
	protected QuoteImportService quoteImportService;
	@MockitoBean
	protected PortfolioToIndexService portfolioToIndexService;
	@MockitoBean
	protected CurrencyService currencyService;
	@MockitoBean
	protected SymbolImportService symbolImportService;
	@MockitoBean
	protected KafkaTemplate kafkaTemplate;
	
	@MockitoBean
	protected JpaAppUserRepository jpaAppUserRepository;
	@MockitoBean
	protected JpaCurrencyRepository jpaCurrencyRepository;
	@MockitoBean
	protected JpaDailyQuoteRepository jpaDailyQuoteRepository;
	@MockitoBean
	protected JpaFinancialElementRepository jpaFinancialElementRepository;
	@MockitoBean
	protected JpaPortfolioElementRepository jpaPortfolioElementRepository;
	@MockitoBean
	protected JpaPortfolioRepository jpaPortfolioRepository;
	@MockitoBean
	protected JpaPortfolioToSymbolRepository jpaPortfolioToSymbolRepository;
	@MockitoBean 
	protected JpaRevokedTokenRepository jpaRevokedTokenRepository;
	@MockitoBean
	protected JpaSectorRepository jpaSectorRepository;
	@MockitoBean
	protected JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository;
	@MockitoBean
	protected EntityManager entityManager;
	@MockitoBean
	protected JpaSymbolRepository jpaSymbolRepository;
	@MockitoBean
	protected DataSource dataSource;
	@MockitoBean
	protected EventPublicationsBean eventPublicationsBean;
	
}
