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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSortedMap;

import ch.xxx.manager.domain.exception.ResourceNotFoundException;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.CurrencyRepository;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class PortfolioCalculationService {
	private record PortfolioElement(Long symbolId, LocalDate localDate, BigDecimal value) {
	};

	private static final Logger LOG = LoggerFactory.getLogger(PortfolioCalculationService.class);
	private final DailyQuoteRepository dailyQuoteRepository;
	private final CurrencyRepository currencyRepository;
	private ImmutableSortedMap<LocalDate, Collection<Currency>> currencyMap = ImmutableSortedMap.of();

	public PortfolioCalculationService(DailyQuoteRepository dailyQuoteRepository,
			CurrencyRepository currencyRepository) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyRepository = currencyRepository;
	}

	@PostConstruct
	public void initCurrencyMap() {
		LOG.info("CurrencyMap updated.");
		this.currencyMap = ImmutableSortedMap.copyOf(
				this.currencyRepository.findAll().stream().collect(Collectors.groupingBy(Currency::getLocalDay)));
	}

	public Portfolio calculatePortfolio(Portfolio portfolio) {
		Optional.ofNullable(portfolio).orElseThrow(() -> new ResourceNotFoundException("Portfolio not found."));
		LOG.info("Portfolio calculation called for: {}", portfolio.getId());
		List<PortfolioToSymbol> portfolioToSymbols = List.copyOf(portfolio.getPortfolioToSymbols());
		Map<Long, List<DailyQuote>> dailyQuotesMap = this.dailyQuoteRepository
				.findBySymbolIds(portfolioToSymbols.stream().map(mySymbol -> mySymbol.getSymbol().getId())
						.collect(Collectors.toList()))
				.stream().collect(Collectors.groupingBy(myDailyQuote -> myDailyQuote.getSymbol().getId()));
		List<PortfolioElement> portfolioElements = portfolioToSymbols.stream()
				.map(pts -> this.calcPortfolioElementsForSymbol(pts, dailyQuotesMap.get(pts.getSymbol().getId())))
				.flatMap(Collection::stream).sorted(Comparator.comparing(PortfolioElement::localDate))
				.collect(Collectors.toList());
		LocalDate cutOffDate = LocalDate.now().minus(Period.ofMonths(1));
		portfolio.setMonth1(portfolioValueAtDate(portfolioToSymbols, portfolioElements, cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofMonths(6));
		portfolio.setMonth6(portfolioValueAtDate(portfolioToSymbols, portfolioElements, cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(1));
		portfolio.setYear1(portfolioValueAtDate(portfolioToSymbols, portfolioElements, cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(2));
		portfolio.setYear2(portfolioValueAtDate(portfolioToSymbols, portfolioElements, cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(5));
		portfolio.setYear5(portfolioValueAtDate(portfolioToSymbols, portfolioElements, cutOffDate));
		cutOffDate = LocalDate.now().minus(Period.ofYears(10));
		portfolio.setYear10(portfolioValueAtDate(portfolioToSymbols, portfolioElements, cutOffDate));
		return portfolio;
	}

	private BigDecimal portfolioValueAtDate(List<PortfolioToSymbol> portfolioToSymbols,
			List<PortfolioElement> portfolioElements, LocalDate cutOffDate) {
		BigDecimal result = portfolioToSymbols.stream()
				.map(pts -> findValueAtDate(portfolioElements, cutOffDate, pts.getSymbol().getId()))
				.filter(Optional::isPresent).map(pe -> pe.get()).map(pe -> pe.value)
				.reduce(BigDecimal.ZERO, (acc, value) -> acc.add(value));
		return result;
	}

	private Optional<PortfolioElement> findValueAtDate(List<PortfolioElement> portfolioElements, LocalDate cutOffDate,
			Long symbolId) {
		return portfolioElements.stream().filter(pts -> pts.symbolId.equals(symbolId))
				.filter(pts -> pts.localDate().isBefore(cutOffDate))
				.max(Comparator.comparing(PortfolioElement::localDate));
	}

	private Collection<PortfolioElement> calcPortfolioElementsForSymbol(PortfolioToSymbol portfolioToSymbol,
			List<DailyQuote> dailyQuotes) {
		return dailyQuotes.stream()
				.filter(myDailyQuote -> portfolioToSymbol.getChangedAt().compareTo(myDailyQuote.getLocalDay()) <= 0
						&& Optional.ofNullable(portfolioToSymbol.getRemovedAt()).stream()
								.filter(myRemovedAt -> myDailyQuote.getLocalDay().compareTo(myRemovedAt) >= 0)										
								.findAny().isEmpty())
				.map(myDailyQuote -> this.calculatePortfolioElement(myDailyQuote, portfolioToSymbol))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	private Optional<PortfolioElement> calculatePortfolioElement(DailyQuote dailyQuote,
			PortfolioToSymbol portfolioToSymbol) {
		return getCurrencyQuote(portfolioToSymbol, dailyQuote)
				.map(currencyQuote -> new PortfolioElement(portfolioToSymbol.getSymbol().getId(),
						dailyQuote.getLocalDay(), currencyQuote.getClose().multiply(dailyQuote.getClose())));

	}

	private Optional<Currency> getCurrencyQuote(PortfolioToSymbol portfolioToSymbol, DailyQuote myDailyQuote) {
//		LOG.info(myDailyQuote.getLocalDay().format(DateTimeFormatter.ISO_LOCAL_DATE));		
		return LongStream.range(0, 7).boxed()
				.map(minusDays -> Optional
						.ofNullable(this.currencyMap.get(myDailyQuote.getLocalDay().minusDays(minusDays)))
						.orElse(List.of()).stream()
						.filter(myCurrency -> portfolioToSymbol.getPortfolio().getCurrencyKey()
								.equals(myCurrency.getFromCurrKey())
								&& portfolioToSymbol.getSymbol().getCurrencyKey().equals(myCurrency.getToCurrKey()))
						.findFirst())
				.filter(Optional::isPresent).map(Optional::get).findFirst();
	}
}
