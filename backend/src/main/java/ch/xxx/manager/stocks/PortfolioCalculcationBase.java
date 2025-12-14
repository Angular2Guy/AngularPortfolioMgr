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
package ch.xxx.manager.stocks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.xxx.manager.common.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.xxx.manager.stocks.entity.Currency;
import ch.xxx.manager.stocks.entity.DailyQuote;
import ch.xxx.manager.stocks.entity.DailyQuoteRepository;
import ch.xxx.manager.stocks.entity.Portfolio;
import ch.xxx.manager.stocks.entity.PortfolioToSymbol;
import ch.xxx.manager.stocks.entity.Symbol;
import ch.xxx.manager.stocks.entity.SymbolRepository;
import ch.xxx.manager.common.utils.StreamHelpers;

public abstract class PortfolioCalculcationBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioCalculcationBase.class);
	protected final DailyQuoteRepository dailyQuoteRepository;
	protected final SymbolRepository symbolRepository;
	protected final CurrencyService currencyService;

	public PortfolioCalculcationBase(DailyQuoteRepository dailyQuoteRepository, CurrencyService currencyService,
			SymbolRepository symbolRepository) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyService = currencyService;
		this.symbolRepository = symbolRepository;
	}

	protected Map<String, List<DailyQuote>> createDailyQuotesKeyMap(Set<PortfolioToSymbol> portfolioToSymbols) {
		return portfolioToSymbols.stream().filter(pts -> pts.getRemovedAt() == null).filter(pts -> pts.getWeight() > 0).map(PortfolioToSymbol::getSymbol)
				.filter(StreamHelpers.distinctByKey(mySymbol -> mySymbol))
				.collect(Collectors.toMap(Symbol::getSymbol,
						mySymbol -> mySymbol.getDailyQuotes().stream()
								.filter(myQuote -> mySymbol.getSymbol().equalsIgnoreCase(myQuote.getSymbolKey()))
								.collect(Collectors.toList())));
	}

	protected Map<String, List<DailyQuote>> createDailyQuotesSymbolKeyMap(List<String> symbolStrs) {
		Map<String, List<DailyQuote>> dailyQuotesMap = this.dailyQuoteRepository.findBySymbolKeys(symbolStrs).stream()
				.sorted(Comparator.comparing(DailyQuote::getSymbolKey))
				.filter(StreamHelpers.distinctByKey(DailyQuote::getSymbolKey))
				.sorted(Comparator.comparing(DailyQuote::getLocalDay))
				.collect(Collectors.groupingBy(DailyQuote::getSymbolKey));
		final record MyKeyValue(String key, List<DailyQuote> quotes) {
		}
		final Map<String, List<DailyQuote>> filteredDailyQuotesMap = dailyQuotesMap.keySet().stream()
				.map(myKey -> new MyKeyValue(myKey,
						dailyQuotesMap.get(myKey).stream().filter(StreamHelpers.distinctByKey(DailyQuote::getLocalDay))
								.sorted(Comparator.comparing(DailyQuote::getLocalDay)).collect(Collectors.toList())))
				.collect(Collectors.toMap(MyKeyValue::key, MyKeyValue::quotes));
		return filteredDailyQuotesMap;
	}

	protected BigDecimal calcValue(Function<? super Currency, BigDecimal> currExtractor, Currency currencyQuote,
			Function<? super DailyQuote, BigDecimal> quoteExtractor, DailyQuote dailyQuote, final Portfolio portfolio) {
		final BigDecimal currValue = Optional.ofNullable(currExtractor.apply(currencyQuote)).orElse(BigDecimal.ZERO);
		final BigDecimal quoteValue = Optional.ofNullable(quoteExtractor.apply(dailyQuote)).orElse(BigDecimal.ZERO);
		BigDecimal calcValue = quoteValue;
		if (dailyQuote.getCurrencyKey().equals(currencyQuote.getFromCurrKey())
				&& portfolio.getCurrencyKey().equals(currencyQuote.getToCurrKey())) {
			calcValue = quoteValue.multiply(currValue);
		} else if (dailyQuote.getCurrencyKey().equals(currencyQuote.getToCurrKey())
				&& portfolio.getCurrencyKey().equals(currencyQuote.getFromCurrKey())) {
			calcValue = quoteValue.divide(currValue, 10, RoundingMode.HALF_UP);
		} else if (!dailyQuote.getCurrencyKey().equals(portfolio.getCurrencyKey())) {
			LOGGER.info("calcValue at {} symbol: {} dailyQuote: {} portfolio: {} not found.",
					dailyQuote.getLocalDay().toString(), dailyQuote.getSymbolKey(), dailyQuote.getCurrencyKey().name(),
					portfolio.getCurrencyKey().name());
			LOGGER.info("calcValue currencyQuote at {} value: {} from: {} to: {}",
					currencyQuote.getLocalDay().toString(), currencyQuote.getClose().toString(),
					currencyQuote.getFromCurrKey().name(), currencyQuote.getToCurrKey().name());
		}
		return calcValue;
	}

	protected List<LocalDate> filteredCommonQuoteDates(Map<String, List<DailyQuote>> dailyQuotesKeyMap) {
		final Set<LocalDate> quoteDates = dailyQuotesKeyMap.keySet().stream().map(myId -> dailyQuotesKeyMap.get(myId))
				.flatMap(List::stream).map(DailyQuote::getLocalDay).collect(Collectors.toSet());
		final List<LocalDate> commonQuoteDates = quoteDates.stream()
				.filter(myLocalDate -> dailyQuotesKeyMap.keySet().stream()
						.filter(myKey -> !myKey.contains(ServiceUtils.PORTFOLIO_MARKER))
						.map(myId -> dailyQuotesKeyMap.get(myId).stream()
								.anyMatch(myQuote -> myLocalDate.isEqual(myQuote.getLocalDay())))
						.allMatch(myResult -> myResult.equals(Boolean.TRUE)))
				.collect(Collectors.toSet()).stream().sorted().toList();
		List<LocalDate> allDates = dailyQuotesKeyMap.values().stream().flatMap(List::stream)
				.map(DailyQuote::getLocalDay).toList();
		return dailyQuotesKeyMap.keySet().size() == 1L ? allDates
				: commonQuoteDates.stream().filter(
						myLocalDate -> !LocalDate.now().equals(myLocalDate)).toList();
	}

	public Portfolio addDailyQuotes(Portfolio portfolio) {
		portfolio.getPortfolioToSymbols().forEach(pts -> {
			// LOGGER.info("Symbol Id: {}", pts.getSymbol().getId());
			pts.setSymbol(this.symbolRepository.findByIdWithDailyQuotes(pts.getSymbol().getId())
					.orElse(this.symbolRepository.findById(pts.getSymbol().getId()).orElseThrow()));
		});
		return portfolio;
	}	
}
