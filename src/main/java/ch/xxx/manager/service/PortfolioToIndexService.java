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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.dto.QuoteDto;
import ch.xxx.manager.entity.CurrencyEntity;
import ch.xxx.manager.entity.DailyQuoteEntity;
import ch.xxx.manager.entity.PortfolioToSymbolEntity;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.entity.SymbolEntity.SymbolCurrency;
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

	public Flux<QuoteDto> calculateIndexComparison(Long portfolioId, ComparisonIndex comparisonIndex) {
		LOGGER.info("CalculateComparison Index: {} for PortfolioId: {}", comparisonIndex.getName(), portfolioId);
		return this.currencyRepository.findAll()
				.collectMultimap(entity -> entity.getLocalDay().toString(), entity -> entity)
				.flatMap(currencyMap -> this.portfolioToSymbolRepository.findByPortfolioId(portfolioId).collectList()
						.flatMap(ptsEntities -> this.symbolRepository
								.findAllById(ptsEntities.stream().map(ptsEntity -> ptsEntity.getSymbolId()).distinct()
										.collect(Collectors.toList()))
								.flatMap(symbolEntity -> this.dailyQuoteRepository
										.findBySymbol(symbolEntity.getSymbol()).collectList()
										.flatMap(dailyQuoteEntities -> this.calculateIndexes(ptsEntities, symbolEntity,
												dailyQuoteEntities, currencyMap, comparisonIndex).collectList()))
								.collectList()))
				.flatMapMany(Flux::fromIterable).flatMap(Flux::fromIterable).collectList().flatMap(entities -> {
					LOGGER.info("List filer called. Size: {}", entities.size());
					return Mono.just(entities.stream().filter(entity -> entity.getLocalDay() != null)
							.filter(ServiceUtils.distinctByKey(entity -> entity.getLocalDay().toString()))
							.collect(Collectors.toList()));
				}).flux().flatMap(Flux::fromIterable).flatMap(entity -> this.mapToDto(entity));
	}

	public Flux<QuoteDto> calculateIndexComparison(Long portfolioId, ComparisonIndex comparisonIndex, LocalDate from,
			LocalDate to) {
		return this.calculateIndexComparison(portfolioId, comparisonIndex)
				.filter(quoteDto -> quoteDto.getClose() != null && 0 < quoteDto.getClose().compareTo(BigDecimal.ZERO))
				.filter(quoteDto -> -1 < quoteDto.getTimestamp().compareTo(from.atStartOfDay())
						&& 1 > quoteDto.getTimestamp().compareTo(LocalDateTime.from(to.atTime(23, 59))));
	}

	private Flux<QuoteDto> mapToDto(DailyQuoteEntity entity) {
		LOGGER.info(entity.toString());
		QuoteDto dto = new QuoteDto();
		dto.setClose(entity.getClose());
		dto.setSymbol(entity.getSymbol());
		dto.setTimestamp(entity.getLocalDay().atStartOfDay());
		dto.setVolume(entity.getVolume());
		return Flux.fromIterable(List.of(dto));
	}

	private Flux<DailyQuoteEntity> upsertIndexComparisonDate(Tuple3<LocalDate, BigDecimal[], BigDecimal> tuple,
			Map<LocalDate, DailyQuoteEntity> comparisonIndexQuoteMap,
			Map<String, DailyQuoteEntity> currentSymbolQuoteMap, Map<String, Collection<CurrencyEntity>> currencyMap) {
		Optional<DailyQuoteEntity> currentQuoteOpt = Optional
				.ofNullable(currentSymbolQuoteMap.get(tuple.getA().toString()))
				.filter(symbolQuote -> symbolQuote.getClose() != null && symbolQuote.getCurrencyId() != null);
		Optional<DailyQuoteEntity> comparisonQuoteOpt = Optional.ofNullable(comparisonIndexQuoteMap.get(tuple.getA()))
				.filter(indexQuote -> indexQuote.getClose() != null && indexQuote.getCurrencyId() != null);
		if (comparisonQuoteOpt.isEmpty()) {
			return Flux.empty();
		}
		SymbolCurrency symbolCurrency = List.of(ComparisonIndex.values()).stream()
				.filter(compIndex -> compIndex.getSymbol().equals(comparisonQuoteOpt.get().getSymbol())).findFirst()
				.map(compIndex -> compIndex.getCurrency()).orElseThrow();
		Optional<CurrencyEntity> currencyQuoteOpt = currencyMap.get(tuple.getA().toString()) == null ? Optional.empty()
				: currencyMap.get(tuple.getA().toString()).stream()
						.filter(currencyEntity -> symbolCurrency.toString().equals(currencyEntity.getTo_curr()))
						.findFirst();
		if (!symbolCurrency.equals(SymbolCurrency.EUR) && currencyQuoteOpt.isEmpty()) {
			return Flux.empty();
		}
		BigDecimal comparisonQuoteEur = comparisonQuoteOpt.get().getClose().multiply(currencyQuoteOpt.get().getClose());
		BigDecimal newPortfolioShares = tuple.getB()[0].add(tuple.getC().compareTo(BigDecimal.valueOf(0.0001)) > 0
				? comparisonQuoteEur.divide(tuple.getC(), 10, RoundingMode.HALF_UP)
				: BigDecimal.ZERO);
		tuple.getB()[0] = newPortfolioShares;
		DailyQuoteEntity newDailyQuoteEntity = currentQuoteOpt.orElse(new DailyQuoteEntity());
		newDailyQuoteEntity.setCurrencyId(currencyQuoteOpt.get().getId());
		newDailyQuoteEntity.setClose(newPortfolioShares.multiply(comparisonQuoteOpt.get().getClose()));
		newDailyQuoteEntity.setLocalDay(tuple.getA());
		newDailyQuoteEntity.setSymbol(comparisonQuoteOpt.get().getSymbol());
		newDailyQuoteEntity.setSymbolId(comparisonQuoteOpt.get().getSymbolId());
		newDailyQuoteEntity.setVolume(comparisonQuoteOpt.get().getVolume());

		return Flux.just(newDailyQuoteEntity);
	}

	private Flux<DailyQuoteEntity> calculateIndexes(List<PortfolioToSymbolEntity> ptsEntities,
			SymbolEntity symbolEntity, List<DailyQuoteEntity> dailyQuoteEntities,
			Map<String, Collection<CurrencyEntity>> currencyMap, ComparisonIndex comparisonIndex) {
		final Map<String, DailyQuoteEntity> symbolQuoteMap = dailyQuoteEntities.stream()
				.collect(Collectors.toMap(entity -> entity.getLocalDay().toString(), entity -> entity));
		final List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> sortedPortfolioChanges = this
				.calculateSortedPortfolioChanges(ptsEntities, symbolEntity, dailyQuoteEntities);
		Mono<Map<LocalDate, DailyQuoteEntity>> comparisonIndexQuotes = this.dailyQuoteRepository
				.findBySymbol(comparisonIndex.getSymbol()).collectMap(entity -> entity.getLocalDay());
		return comparisonIndexQuotes.flatMapMany(quoteMap -> this.recalculateComparisonIndex(quoteMap, symbolQuoteMap,
				sortedPortfolioChanges, currencyMap, symbolEntity));
	}

	private Flux<DailyQuoteEntity> recalculateComparisonIndex(Map<LocalDate, DailyQuoteEntity> comparisonIndexQuoteMap,
			Map<String, DailyQuoteEntity> currentSymbolQuoteMap,
			List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> sortedPortfolioChanges,
			Map<String, Collection<CurrencyEntity>> currencyMap, SymbolEntity symbolEntity) {
		final List<LocalDate> comparisonIndexDates = comparisonIndexQuoteMap.keySet().stream().sorted()
				.collect(Collectors.toList());
		final BigDecimal[] portfolioShares = List.of(BigDecimal.ZERO).toArray(new BigDecimal[1]);
		Flux<DailyQuoteEntity> result = Flux.mergeSequential(comparisonIndexDates.stream().flatMap(myDate -> {
//			LOGGER.info("Date: {}", myDate.toString());
			BigDecimal difference = this.updatedPortfolioValue(myDate, currencyMap, sortedPortfolioChanges,
					symbolEntity);
			if (BigDecimal.ONE.compareTo(difference) <= 0) {
				LOGGER.info("Date: {}, difference: {}, symbol: {}", myDate.toString(),difference.toString(),
						symbolEntity.getSymbol());
			}
			return Stream.of(this.upsertIndexComparisonDate(
					new Tuple3<LocalDate, BigDecimal[], BigDecimal>(myDate, portfolioShares, difference),
					comparisonIndexQuoteMap, currentSymbolQuoteMap, currencyMap));
		}).collect(Collectors.toList()));
		return result;
	}

	private BigDecimal updatedPortfolioValue(LocalDate currentDate, Map<String, Collection<CurrencyEntity>> currencyMap,
			List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> portfolioChanges,
			SymbolEntity symbolEntity) {
//		LOGGER.info("CurrentDate {}, ChangeDate {}", currentDate.toString(),
//				portfolioChanges.stream()
//						.filter(tuple3 -> !symbolEntity.getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER)
//								&& tuple3.getB().getSymbol().equals(symbolEntity.getSymbol()))
//						.anyMatch(tuple3 -> currentDate.isEqual(tuple3.getA().getChangedAt())));
		BigDecimal difference = portfolioChanges.stream()
				.filter(tuple3 -> !symbolEntity.getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER)
						&& tuple3.getB().getSymbol().equals(symbolEntity.getSymbol()))
				.filter(tuple3 -> (tuple3.getA().getChangedAt() != null
						&& currentDate.isEqual(tuple3.getA().getChangedAt()))
						|| (tuple3.getA().getRemovedAt() != null && currentDate.isEqual(tuple3.getA().getRemovedAt())))
				.reduce(BigDecimal.ZERO, (value, tuple3) -> {
					BigDecimal newValue = BigDecimal.ZERO;
					if (SymbolCurrency.EUR.toString().equals(tuple3.getB().getCurr())) {
						newValue = BigDecimal.valueOf(tuple3.getA().getWeight()).multiply(tuple3.getC().getClose());
					} else if (currencyMap.containsKey(currentDate.toString())
							&& currencyMap.get(currentDate.toString()).stream().anyMatch(
									currencyEntity -> tuple3.getB().getCurr().equals(currencyEntity.getTo_curr()))) {
						BigDecimal currencyValue = currencyMap.get(currentDate.toString()).stream()
								.filter(currencyEntity -> tuple3.getB().getCurr().equals(currencyEntity.getTo_curr()))
								.map(currencyEntity -> currencyEntity.getClose()).findFirst().orElseThrow();
						newValue = BigDecimal.valueOf(tuple3.getA().getWeight())
								.multiply(tuple3.getC().getClose().multiply(currencyValue));
					}
//					LOGGER.info("NewValue: {}", newValue.toString());
					return value.add(newValue);
				}, BigDecimal::add);
		return difference;
	}

	private List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> calculateSortedPortfolioChanges(
			List<PortfolioToSymbolEntity> ptsEntities, SymbolEntity symbolEntity,
			List<DailyQuoteEntity> dailyQuoteEntities) {
		Map<LocalDate, PortfolioToSymbolEntity> myPtsEntities = ptsEntities.stream()
				.filter(ptsEntity -> ptsEntity.getSymbolId().equals(symbolEntity.getId()))
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
