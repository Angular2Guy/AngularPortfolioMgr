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
package ch.xxx.manager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.entity.CurrencyEntity;
import ch.xxx.manager.entity.DailyQuoteEntity;
import ch.xxx.manager.entity.PortfolioToSymbolEntity;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.entity.SymbolEntity.QuoteSource;
import ch.xxx.manager.entity.SymbolEntity.SymbolCurrency;
import ch.xxx.manager.jwt.Tuple;
import ch.xxx.manager.repository.CurrencyRepository;
import ch.xxx.manager.repository.DailyQuoteRepository;
import ch.xxx.manager.repository.PortfolioToSymbolRepository;
import ch.xxx.manager.repository.SymbolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PortfolioToIndexService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioToIndexService.class);
	@Autowired
	private PortfolioToSymbolRepository portfolioToSymbolRepository;
	@Autowired
	private SymbolRepository symbolRepository;
	@Autowired
	private DailyQuoteRepository dailyQuoteRepository;
	@Autowired
	private CurrencyRepository currencyRepository;

	public Flux<Long> calculateIndexComparison(Long portfolioId, ComparisonIndex comparisonIndex) {
		LOGGER.info("CalculateComparison Index: {} for PortfolioId: {}", comparisonIndex.getName(), portfolioId);
		return this.currencyRepository.findAll().collectMultimap(entity -> entity.getLocalDay(), entity -> entity)
				.flatMap(currencyMap -> this.portfolioToSymbolRepository.findByPortfolioId(portfolioId).collectList()
						.flatMap(ptsEntities -> this.symbolRepository
								.findAllById(ptsEntities.stream().map(ptsEntity -> ptsEntity.getSymbolId()).distinct()
										.collect(Collectors.toList()))
								.flatMap(symbolEntity -> this.dailyQuoteRepository
										.findBySymbol(symbolEntity.getSymbol()).collectList()
										.flatMap(dailyQuoteEntities -> this.calculateIndexes(ptsEntities, symbolEntity,
												dailyQuoteEntities, currencyMap, comparisonIndex))
										.flux())
								.collectList()))
				.flux().flatMap(results -> Flux.just(results.stream().count()));
	}

	private Mono<DailyQuoteEntity> upsertIndexComparisonDate(Tuple<LocalDate, BigDecimal> tuple,
			Map<LocalDate, DailyQuoteEntity> comparisonIndexQuoteMap,
			Map<LocalDate, DailyQuoteEntity> currentIndexQuoteMap,
			Map<LocalDate, Collection<CurrencyEntity>> currencyMap) {
		Optional<DailyQuoteEntity> currentQuoteOpt = Optional.ofNullable(currentIndexQuoteMap.get(tuple.getA()));
		Optional<DailyQuoteEntity> comparisonQuoteOpt = Optional.ofNullable(comparisonIndexQuoteMap.get(tuple.getA()));
		if (comparisonQuoteOpt.isEmpty()) {
			return Mono.empty();
		}
		SymbolCurrency symbolCurrency = List.of(ComparisonIndex.values()).stream()
				.filter(compIndex -> compIndex.getSymbol().equals(comparisonQuoteOpt.get().getSymbol())).findFirst()
				.map(compIndex -> compIndex.getCurrency()).orElseThrow();
		Optional<CurrencyEntity> currencyQuoteOpt = currencyMap.get(tuple.getA()).stream()
				.filter(currencyEntity -> symbolCurrency.toString().equals(currencyEntity.getFrom_curr())).findFirst();
		if (!symbolCurrency.equals(SymbolCurrency.EUR) && currencyQuoteOpt.isEmpty()) {
			return Mono.empty();
		}
		DailyQuoteEntity newDailyQuoteEntity = null;
		if (currentQuoteOpt.isEmpty()) {
			newDailyQuoteEntity = new DailyQuoteEntity();
			newDailyQuoteEntity.setCurrencyId(currencyQuoteOpt.get().getId());
		} else {
			newDailyQuoteEntity = currentQuoteOpt.get();
		}

		return Mono.empty();
	}

	private Mono<Long> calculateIndexes(List<PortfolioToSymbolEntity> ptsEntities, SymbolEntity symbolEntity,
			List<DailyQuoteEntity> dailyQuoteEntities, Map<LocalDate, Collection<CurrencyEntity>> currencyMap,
			ComparisonIndex comparisonIndex) {
		final Map<LocalDate, DailyQuoteEntity> indexQuoteMap = dailyQuoteEntities.stream()
				.collect(Collectors.toMap(DailyQuoteEntity::getLocalDay, entity -> entity));
		final List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> sortedPortfolioChanges = this
				.calculateSortedPortfolioChanges(ptsEntities, symbolEntity, dailyQuoteEntities);
		Mono<Map<LocalDate, DailyQuoteEntity>> comparisonIndexQuotes = this.dailyQuoteRepository.findBySymbol(comparisonIndex.getSymbol())
				.collectMap(DailyQuoteEntity::getLocalDay);		
		return comparisonIndexQuotes.flatMap(quoteMap -> this.recalculateComparisonIndex(quoteMap, indexQuoteMap,
				sortedPortfolioChanges, currencyMap));
	}

	private Mono<Long> recalculateComparisonIndex(Map<LocalDate, DailyQuoteEntity> comparisonIndexQuoteMap,
			Map<LocalDate, DailyQuoteEntity> currentIndexQuoteMap,
			List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> sortedPortfolioChanges,
			Map<LocalDate, Collection<CurrencyEntity>> currencyMap) {
		final List<LocalDate> comparisonIndexDates = comparisonIndexQuoteMap.keySet().stream()
				.filter(myLocalDate -> -1 > myLocalDate.compareTo(sortedPortfolioChanges.get(0).getA().getChangedAt()))
				.sorted().collect(Collectors.toList());
		final Map<LocalDate, DailyQuoteEntity> currentIndexQuotesLeftMap = new HashMap<>(currentIndexQuoteMap);
		final BigDecimal[] portfolioValue = List.of(BigDecimal.ZERO).toArray(new BigDecimal[1]);
		comparisonIndexDates.stream().flatMap(myDate -> {
			portfolioValue[0] = portfolioValue[0]
					.add(this.updatePortfolioValue(myDate, currencyMap, sortedPortfolioChanges));
			return Stream.of(this.upsertIndexComparisonDate(new Tuple<LocalDate, BigDecimal>(myDate, portfolioValue[0]),
					comparisonIndexQuoteMap, currentIndexQuoteMap, currencyMap));
		});
		return Mono.empty();
	}

	private BigDecimal updatePortfolioValue(LocalDate currentDate,
			Map<LocalDate, Collection<CurrencyEntity>> currencyMap,
			List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> portfolioChanges) {
		BigDecimal difference = portfolioChanges.stream()
				.filter(tuple3 -> currentDate.equals(tuple3.getA().getChangedAt())
						|| currentDate.equals(tuple3.getA().getRemovedAt()))
				.reduce(BigDecimal.ZERO, (value, tuple3) -> {
					BigDecimal newValue = BigDecimal.ZERO;
					if (SymbolCurrency.EUR.toString().equals(tuple3.getB().getCurr())) {
						newValue = BigDecimal.valueOf(tuple3.getA().getWeight()).multiply(tuple3.getC().getClose());
					} else if (currencyMap.containsKey(currentDate) && currencyMap.get(currentDate).stream().anyMatch(
							currencyEntity -> tuple3.getB().getCurr().equals(currencyEntity.getFrom_curr()))) {
						BigDecimal currencyValue = currencyMap.get(currentDate).stream()
								.filter(currencyEntity -> tuple3.getB().getCurr().equals(currencyEntity.getFrom_curr()))
								.map(currencyEntity -> currencyEntity.getClose()).findFirst().orElseThrow();
						newValue = BigDecimal.valueOf(tuple3.getA().getWeight())
								.multiply(tuple3.getC().getClose().multiply(currencyValue));
					}
					return value.add(newValue);
				}, BigDecimal::add);
		return difference;
	}

	private List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> calculateSortedPortfolioChanges(
			List<PortfolioToSymbolEntity> ptsEntities, SymbolEntity symbolEntity,
			List<DailyQuoteEntity> dailyQuoteEntities) {
		Map<LocalDate, PortfolioToSymbolEntity> myPtsEntities = ptsEntities.stream()
				.filter(ptsEntity -> ptsEntity.getPortfolioId().equals(symbolEntity.getId()))
				.collect(Collectors.toMap(ptsEntity -> ptsEntity.getChangedAt() != null ? ptsEntity.getChangedAt()
						: ptsEntity.getRemovedAt(), ptsEntity -> ptsEntity));
		Map<LocalDate, DailyQuoteEntity> myDailyQuoteEntities = dailyQuoteEntities.stream()
				.filter(dailyQuoteEntity -> myPtsEntities.containsKey(dailyQuoteEntity.getLocalDay()))
				.collect(Collectors.toMap(dailyQuoteEntity -> dailyQuoteEntity.getLocalDay(),
						dailyQuoteEntity -> dailyQuoteEntity));
		List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> portfolioChanges = myPtsEntities.keySet()
				.stream().sorted()
				.flatMap(myDate -> Stream.of(new Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>(
						myPtsEntities.get(myDate), symbolEntity, myDailyQuoteEntities.get(myDate))))
				.collect(Collectors.toList());
		return portfolioChanges;
	}
}
