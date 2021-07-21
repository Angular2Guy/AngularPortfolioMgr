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
package ch.xxx.manager.usecase.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSortedMap;

import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.CurrencyRepository;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioRepository;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbolRepository;

@Service
@Transactional
public class PortfolioCalculationService {
	private record PortfolioElement(Long symbolId, LocalDate localDate, BigDecimal value) {
	};

	private static final Logger LOG = LoggerFactory.getLogger(PortfolioCalculationService.class);
	private final PortfolioRepository portfolioRepository;
	private final DailyQuoteRepository dailyQuoteRepository;
	private final CurrencyRepository currencyRepository;
	private final PortfolioToSymbolRepository portfolioAndSymbolRepository;
	private ImmutableSortedMap<LocalDate, Collection<Currency>> currencyMap = ImmutableSortedMap.of();
	private LocalDateTime lastCurrencyUpdate;

	public PortfolioCalculationService(PortfolioRepository portfolioRepository,
			DailyQuoteRepository dailyQuoteRepository, CurrencyRepository currencyRepository,
			PortfolioToSymbolRepository portfolioAndSymbolRepository) {
		this.portfolioRepository = portfolioRepository;
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyRepository = currencyRepository;
		this.portfolioAndSymbolRepository = portfolioAndSymbolRepository;
	}

	@PostConstruct
	public void initCurrencyMap() {
		Optional<LocalDateTime> localDateTimeOpt = Optional.ofNullable(this.lastCurrencyUpdate)
				.filter(myDate -> LocalDateTime.now().plusHours(1).isAfter(myDate));
		localDateTimeOpt.orElseGet(() -> {
			this.currencyMap = ImmutableSortedMap.copyOf(
					this.currencyRepository.findAll().stream().collect(Collectors.groupingBy(Currency::getLocalDay)));
			return LocalDate.now().atStartOfDay();
		});
		
	}

	public Portfolio calculatePortfolio(Long portfolioId) {
		List<PortfolioToSymbol> portfolioToSymbols = this.portfolioAndSymbolRepository
				.findPortfolioCalcEntitiesByPortfolioId(portfolioId);
		Map<Long, List<DailyQuote>> dailyQuotesMap = this.dailyQuoteRepository
				.findBySymbolIds(portfolioToSymbols.stream().map(mySymbol -> mySymbol.getSymbol().getId())
						.collect(Collectors.toList()))
				.stream().collect(Collectors.groupingBy(myDailyQuote -> myDailyQuote.getSymbol().getId()));

		return null;
	}

	private Collection<PortfolioElement> calcPortfolioElementsForSymbol(PortfolioToSymbol portfolioToSymbol,
			List<DailyQuote> dailyQuotes) {

		return null;
	}
}
