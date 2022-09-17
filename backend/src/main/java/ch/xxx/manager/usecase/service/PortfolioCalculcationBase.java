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
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;

public abstract class PortfolioCalculcationBase {
	protected final DailyQuoteRepository dailyQuoteRepository;
	protected final CurrencyService currencyService;

	public PortfolioCalculcationBase(DailyQuoteRepository dailyQuoteRepository, CurrencyService currencyService) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyService = currencyService;
	}

	protected Map<Long, List<DailyQuote>> createDailyQuotesIdMap(List<PortfolioToSymbol> portfolioToSymbols) {
		Map<Long, List<DailyQuote>> dailyQuotesMap = this.dailyQuoteRepository
				.findBySymbolIds(portfolioToSymbols.stream().map(mySymbol -> mySymbol.getSymbol().getId()).toList())
				.stream().sorted(Comparator.comparing(DailyQuote::getLocalDay))
				.collect(Collectors.groupingBy(myDailyQuote -> myDailyQuote.getSymbol().getId()));
		return dailyQuotesMap;
	}

	protected Map<String, List<DailyQuote>> createDailyQuotesSymbolKeyMap(List<String> symbolStrs) {
		Map<String, List<DailyQuote>> dailyQuotesMap = this.dailyQuoteRepository.findBySymbolKeys(symbolStrs).stream()
				.sorted(Comparator.comparing(DailyQuote::getLocalDay))
				.collect(Collectors.groupingBy(myDailyQuote -> myDailyQuote.getSymbolKey()));
		return dailyQuotesMap;
	}

	protected BigDecimal calcValue(Function<? super Currency, BigDecimal> currExtractor, Currency currencyQuote,
			Function<? super DailyQuote, BigDecimal> quoteExtractor, DailyQuote dailyQuote, final Portfolio portfolio) {
		final BigDecimal currValue = currExtractor.apply(currencyQuote);
		final BigDecimal quoteValue = quoteExtractor.apply(dailyQuote);
		BigDecimal calcValue = BigDecimal.ZERO;
		if (dailyQuote.getCurrencyKey().equals(currencyQuote.getFromCurrKey())
				&& portfolio.getCurrencyKey().equals(currencyQuote.getToCurrKey())) {
			calcValue = quoteValue.multiply(currValue);
		} else if (dailyQuote.getCurrencyKey().equals(currencyQuote.getToCurrKey())
				&& portfolio.getCurrencyKey().equals(currencyQuote.getFromCurrKey())) {
			calcValue = quoteValue.divide(currValue, 10, RoundingMode.HALF_UP);
		}
		return calcValue;
	}
}
