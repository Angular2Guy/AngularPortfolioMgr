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
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSortedMap;

import ch.xxx.manager.domain.model.dto.DailyFxQuoteImportDto;
import ch.xxx.manager.domain.model.dto.DailyFxWrapperImportDto;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.CurrencyRepository;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.utils.CurrencyKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CurrencyService {
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
	private final CurrencyRepository currencyRepository;
	private final AlphavatageClient alphavatageClient;
	private ImmutableSortedMap<LocalDate, Collection<Currency>> currencyMap = ImmutableSortedMap.of();

	public CurrencyService(CurrencyRepository currencyRepository, AlphavatageClient alphavatageClient) {
		this.currencyRepository = currencyRepository;
		this.alphavatageClient = alphavatageClient;
	}

	@PostConstruct
	public void initCurrencyMap() {
		LOG.info("CurrencyMap updated.");
		this.currencyMap = ImmutableSortedMap.copyOf(
				this.currencyRepository.findAll().stream().collect(Collectors.groupingBy(Currency::getLocalDay)));
	}

	public Long importFxDailyQuoteHistory(String to_currency) {
		LOG.info("importFxDailyQuoteHistory() called to currency: {}", to_currency);
		return Flux.fromIterable(this.currencyRepository.findAll())
				.collectMultimap(entity -> entity.getLocalDay(), entity -> entity)
				.flatMap(myCurrencyMap -> Mono.just(this.currencyRepository
						.saveAll(this.convert(this.alphavatageClient.getFxTimeseriesDailyHistory(to_currency, true)
//										.delayElement(Duration.ofSeconds(3))
								.block(Duration.ofSeconds(13)), myCurrencyMap))
						.size()))
				.blockOptional(Duration.ofSeconds(10)).orElse(0).longValue();
	}

	private List<Currency> convert(DailyFxWrapperImportDto wrapperDto,
			Map<LocalDate, Collection<Currency>> myCurrencyMap) {
		LOG.info("" + wrapperDto.getDailyQuotes().size());
		return wrapperDto.getDailyQuotes().entrySet().stream().flatMap(
				entry -> Stream.of(this.convert(entry, CurrencyKey.valueOf(wrapperDto.getMetadata().getFromSymbol()),
						CurrencyKey.valueOf(wrapperDto.getMetadata().getToSymbol()))))
				.filter(entity -> myCurrencyMap.get(entity.getLocalDay()) == null
						|| myCurrencyMap.get(entity.getLocalDay()).stream()
								.anyMatch(mapEntity -> entity.getToCurrKey().equals(mapEntity.getToCurrKey())))
				.collect(Collectors.toList());
	}

	private Currency convert(Entry<String, DailyFxQuoteImportDto> entry, CurrencyKey from_curr, CurrencyKey to_curr) {
		return new Currency(LocalDate.parse(entry.getKey(), DateTimeFormatter.ofPattern("yyyy-MM-dd")), from_curr,
				to_curr, new BigDecimal(entry.getValue().getOpen()), new BigDecimal(entry.getValue().getHigh()),
				new BigDecimal(entry.getValue().getLow()), new BigDecimal(entry.getValue().getClose()));
	}

	public Optional<Currency> getCurrencyQuote(PortfolioToSymbol portfolioToSymbol, DailyQuote myDailyQuote) {
		return this.getCurrencyQuote(myDailyQuote.getLocalDay(), portfolioToSymbol);
	}

	public Optional<Currency> getCurrencyQuote(LocalDate day, PortfolioToSymbol portfolioToSymbol) {
		return LongStream.range(0, 7).boxed()
				.map(minusDays -> Optional.ofNullable(this.currencyMap.get(day)).orElse(List.of()).stream()
//						.peek(myCurrency -> LOG.info("symbol: "+portfolioToSymbol.getSymbol().getSymbol()+" from: " + myCurrency.getFromCurrKey() + " to: "
//								+ myCurrency.getToCurrKey() + " ptsCur: " + portfolioToSymbol.getPortfolio().getCurrencyKey()
//								+ " symCur: " + portfolioToSymbol.getSymbol().getCurrencyKey()))
						.filter(myCurrency -> portfolioToSymbol.getPortfolio().getCurrencyKey()
								.equals(myCurrency.getFromCurrKey())
								&& portfolioToSymbol.getSymbol().getCurrencyKey().equals(myCurrency.getToCurrKey()))
						.findFirst()
						.or(() -> Optional.ofNullable(this.currencyMap.get(day)).orElse(List.of()).stream()
//								.peek(myCurrency -> LOG
//										.info("symbol: "+portfolioToSymbol.getSymbol().getSymbol()+" from: " + myCurrency.getFromCurrKey() + " to: " + myCurrency.getToCurrKey()
//												+ " ptsCur: " + portfolioToSymbol.getPortfolio().getCurrencyKey() + " symCur: "
//												+ portfolioToSymbol.getSymbol().getCurrencyKey()))
								.filter(myCurrency -> portfolioToSymbol.getPortfolio().getCurrencyKey()
										.equals(myCurrency.getToCurrKey())
										&& portfolioToSymbol.getSymbol().getCurrencyKey().equals(myCurrency.getFromCurrKey()))
								.map(myCurr -> new Currency(myCurr.getLocalDay(), myCurr.getToCurrKey(),
										myCurr.getFromCurrKey(), BigDecimal.ONE.divide(myCurr.getOpen()),
										BigDecimal.ONE.divide(myCurr.getHigh()), BigDecimal.ONE.divide(myCurr.getLow()),
										BigDecimal.ONE.divide(myCurr.getClose())))
								.findFirst())).filter(Optional::isPresent).map(Optional::get).findFirst();
	}

	public ImmutableSortedMap<LocalDate, Collection<Currency>> getCurrencyMap() {
		return currencyMap;
	}
}
