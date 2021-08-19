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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.exception.ResourceNotFoundException;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.Symbol;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class PortfolioCalculationService {
	private record PortfolioSymbolWithDailyQuotes(Symbol symbol, List<DailyQuote> dailyQuotes) {
	};

	private record PortfolioElement(Long symbolId, LocalDate localDate, BigDecimal value) {
	};

	private static final Logger LOG = LoggerFactory.getLogger(PortfolioCalculationService.class);
	private final DailyQuoteRepository dailyQuoteRepository;
	private final CurrencyService currencyService;

	public PortfolioCalculationService(DailyQuoteRepository dailyQuoteRepository,
			CurrencyService currencyService) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyService = currencyService;
	}

	public Portfolio calculatePortfolio(Portfolio portfolio) {
		Optional.ofNullable(portfolio).orElseThrow(() -> new ResourceNotFoundException("Portfolio not found."));
		LOG.info("Portfolio calculation called for: {}", portfolio.getId());
		List<PortfolioToSymbol> portfolioToSymbols = List.copyOf(portfolio.getPortfolioToSymbols());
		Map<Long, List<DailyQuote>> dailyQuotesMap = this.dailyQuoteRepository
				.findBySymbolIds(portfolioToSymbols.stream().map(mySymbol -> mySymbol.getSymbol().getId())
						.collect(Collectors.toList()))
				.stream().collect(Collectors.groupingBy(myDailyQuote -> myDailyQuote.getSymbol().getId()));
		PortfolioSymbolWithDailyQuotes portfolioQuotes = portfolioToSymbols.stream()
				.filter(pts -> pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.peek(pts -> LOG.info(pts.getSymbol().getSymbol() + " " + pts.getSymbol().getId()))
				.map(pts -> new PortfolioSymbolWithDailyQuotes(pts.getSymbol(),
						Optional.ofNullable(dailyQuotesMap.get(pts.getSymbol().getId())).stream()
								.flatMap(Collection::stream).map(myDailyQuote -> this.resetPortfolioQuote(myDailyQuote))
								.collect(Collectors.toList())))
				.findFirst().orElseThrow(() -> new ResourceNotFoundException("Portfolio Symbol not found."));
		List<PortfolioElement> portfolioElements = portfolioToSymbols.stream()
				.filter(pts -> !pts.getSymbol().getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER))
				.map(pts -> this.calcPortfolioQuotesForSymbol(pts, dailyQuotesMap.get(pts.getSymbol().getId()),
						portfolioQuotes))
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

	private DailyQuote resetPortfolioQuote(DailyQuote myDailyQuote) {
		myDailyQuote.setClose(BigDecimal.ZERO);
		myDailyQuote.setHigh(BigDecimal.ZERO);
		myDailyQuote.setLow(BigDecimal.ZERO);
		myDailyQuote.setOpen(BigDecimal.ZERO);
		return myDailyQuote;
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

	private Collection<PortfolioElement> calcPortfolioQuotesForSymbol(PortfolioToSymbol portfolioToSymbol,
			List<DailyQuote> dailyQuotes, PortfolioSymbolWithDailyQuotes portfolioQuotes) {
		return dailyQuotes.stream()
				.filter(myDailyQuote -> portfolioToSymbol.getChangedAt().compareTo(myDailyQuote.getLocalDay()) <= 0
						&& Optional.ofNullable(portfolioToSymbol.getRemovedAt()).stream()
								.filter(myRemovedAt -> myDailyQuote.getLocalDay().compareTo(myRemovedAt) >= 0).findAny()
								.isEmpty())
				.map(myDailyQuote -> this.calculatePortfolioElement(myDailyQuote, portfolioToSymbol, portfolioQuotes))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	private Optional<PortfolioElement> calculatePortfolioElement(DailyQuote dailyQuote,
			PortfolioToSymbol portfolioToSymbol, PortfolioSymbolWithDailyQuotes portfolioQuotes) {
		return this.currencyService.getCurrencyQuote(portfolioToSymbol, dailyQuote).map(currencyQuote -> {
			DailyQuote myPortfolioQuote = this.upsertPortfolioQuote(currencyQuote, dailyQuote, portfolioToSymbol,
					portfolioQuotes);
			return new PortfolioElement(portfolioToSymbol.getSymbol().getId(), myPortfolioQuote.getLocalDay(),
					myPortfolioQuote.getClose());
		});
	}

	private DailyQuote upsertPortfolioQuote(Currency currencyQuote, DailyQuote dailyQuote,
			PortfolioToSymbol portfolioToSymbol, PortfolioSymbolWithDailyQuotes portfolioQuotes) {
		DailyQuote portfolioQuote = portfolioQuotes.dailyQuotes.stream()
				.filter(myDailyQuote -> myDailyQuote.getLocalDay().isEqual(dailyQuote.getLocalDay())).findFirst()
				.orElse(new DailyQuote());
		portfolioQuote.setClose(this.calcValue(currencyQuote.getClose(), dailyQuote.getClose(), portfolioToSymbol,
				portfolioQuote.getClose()));
		portfolioQuote.setCurrencyKey(portfolioToSymbol.getSymbol().getCurrencyKey());
		portfolioQuote.setHigh(this.calcValue(currencyQuote.getHigh(), dailyQuote.getHigh(), portfolioToSymbol,
				portfolioQuote.getHigh()));
		portfolioQuote.setLocalDay(dailyQuote.getLocalDay());
		portfolioQuote.setLow(this.calcValue(currencyQuote.getLow(), dailyQuote.getLow(), portfolioToSymbol,
				portfolioQuote.getLow()));
		portfolioQuote.setOpen(this.calcValue(currencyQuote.getOpen(), dailyQuote.getOpen(), portfolioToSymbol,
				portfolioQuote.getOpen()));
		portfolioQuote.setSymbol(portfolioQuotes.symbol);
		portfolioQuote.setSymbolKey(portfolioQuotes.symbol.getSymbol());
		portfolioQuote.setVolume(1L);
		if (Optional.ofNullable(portfolioQuote.getId()).isEmpty()) {
			portfolioQuote = this.dailyQuoteRepository.save(portfolioQuote);
			portfolioToSymbol.getSymbol().getDailyQuotes().add(portfolioQuote);
			portfolioQuotes.dailyQuotes.add(portfolioQuote);
		}
		return portfolioQuote;
	}

	private BigDecimal calcValue(BigDecimal currClose, BigDecimal dailyClose, PortfolioToSymbol portfolioToSymbol,
			final BigDecimal portfolioClose) {
		return Optional.ofNullable(portfolioClose).orElse(BigDecimal.ZERO)
				.add(currClose.multiply(dailyClose).multiply(BigDecimal.valueOf(portfolioToSymbol.getWeight())));
	}
}
