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
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.utils.StreamHelpers;

public abstract class PortfolioCalculcationBase {
	protected final DailyQuoteRepository dailyQuoteRepository;
	protected final CurrencyService currencyService;

	public PortfolioCalculcationBase(DailyQuoteRepository dailyQuoteRepository, CurrencyService currencyService) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyService = currencyService;
	}

	protected Map<Long, List<DailyQuote>> createDailyQuotesIdMap(Set<PortfolioToSymbol> portfolioToSymbols) {
		Map<Long, List<DailyQuote>> myDailyQuotesMap = portfolioToSymbols.stream().flatMap(pts -> pts.getSymbol().getDailyQuotes().stream())
				.collect(Collectors.groupingBy(myDailyQuote -> myDailyQuote.getSymbol().getId()));
		final record MyKeyValue(Long id, List<DailyQuote> quotes) {
		}
		Map<Long, List<DailyQuote>> sortedDailyQuotesMap = myDailyQuotesMap.keySet().stream()
				.map(myId -> new MyKeyValue(myId, myDailyQuotesMap.get(myId).stream()
						.sorted(Comparator.comparing(DailyQuote::getLocalDay)).collect(Collectors.toList())))
				.collect(Collectors.toMap(MyKeyValue::id, MyKeyValue::quotes));
		return sortedDailyQuotesMap;
	}

	protected Map<String, List<DailyQuote>> createDailyQuotesSymbolKeyMap(List<String> symbolStrs) {
		Map<String, List<DailyQuote>> dailyQuotesMap = this.dailyQuoteRepository.findBySymbolKeys(symbolStrs).stream()
				.sorted(Comparator.comparing(DailyQuote::getSymbolKey))
				.filter(StreamHelpers.distinctByKey(myQuote -> myQuote.getSymbolKey()))
				.sorted(Comparator.comparing(DailyQuote::getLocalDay))
				.collect(Collectors.groupingBy(myDailyQuote -> myDailyQuote.getSymbolKey()));
		final record MyKeyValue(String key, List<DailyQuote> quotes) {
		}
		final Map<String, List<DailyQuote>> filteredDailyQuotesMap = dailyQuotesMap.keySet().stream()
				.map(myKey -> new MyKeyValue(myKey, dailyQuotesMap.get(myKey).stream()
						.filter(StreamHelpers.distinctByKey(DailyQuote::getLocalDay))
						.sorted(Comparator.comparing(DailyQuote::getLocalDay)).collect(Collectors.toList())))
				.collect(Collectors.toMap(MyKeyValue::key, MyKeyValue::quotes));
		return filteredDailyQuotesMap;
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
	
	protected List<LocalDate> filteredCommonQuoteDates(Map<Long, List<DailyQuote>> dailyQuotesIdMap) {
		final Set<LocalDate> quoteDates = dailyQuotesIdMap.keySet().stream().map(myId -> dailyQuotesIdMap.get(myId))
				.flatMap(List::stream).map(DailyQuote::getLocalDay).collect(Collectors.toSet());
		final List<LocalDate> commonQuoteDates = quoteDates.stream()
				.filter(myLocalDate -> dailyQuotesIdMap.keySet().stream()
						.map(myId -> 
						dailyQuotesIdMap.get(myId).stream().anyMatch(myQuote -> myLocalDate.equals(myQuote.getLocalDay())))
						.allMatch(myResult -> myResult.equals(Boolean.TRUE)))
				.collect(Collectors.toSet()).stream().sorted().toList();
		return commonQuoteDates.stream().filter(myLocalDate -> !LocalDate.now().equals(myLocalDate)).toList();
	}
}
