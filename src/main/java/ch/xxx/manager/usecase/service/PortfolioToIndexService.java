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
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSortedMap;

import ch.xxx.manager.domain.model.dto.QuoteDto;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbolRepository;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.SymbolRepository;

@Service
public class PortfolioToIndexService {
	record QuoteDtoAndWeight(BigDecimal weight, QuoteDto quoteDto) {
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioToIndexService.class);
	private final PortfolioToSymbolRepository portfolioToSymbolRepository;
	private final SymbolRepository symbolRepository;
	private final DailyQuoteRepository dailyQuoteRepository;
	private final CurrencyService currencyService;

	public PortfolioToIndexService(PortfolioToSymbolRepository portfolioToSymbolRepository,
			SymbolRepository symbolRepository, DailyQuoteRepository dailyQuoteRepository,
			CurrencyService currencyService) {
		this.portfolioToSymbolRepository = portfolioToSymbolRepository;
		this.symbolRepository = symbolRepository;
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyService = currencyService;
	}

	public List<QuoteDto> calculateIndexComparison(Long portfolioId, ComparisonIndex comparisonIndex) {
		List<PortfolioToSymbol> portfolioChanges = portfolioToSymbolRepository.findByPortfolioId(portfolioId);
		List<DailyQuote> dailyQuotes = this.dailyQuoteRepository.findBySymbol(comparisonIndex.getSymbol());
		List<QuoteDto> result = compareToIndex(portfolioChanges, dailyQuotes);

		return List.of();
	}

	private List<QuoteDto> compareToIndex(List<PortfolioToSymbol> portfolioChanges, List<DailyQuote> dailyQuotes) {
		record PtsChangesByDay(LocalDate day, PortfolioToSymbol ptsChange, Optional<PortfolioToSymbol> ptsChangeOld) {
		}
		List<PortfolioToSymbol> myPortfolioChanges = portfolioChanges.stream()
				.filter(pts -> Optional.ofNullable(pts.getChangedAt()).isPresent()
						|| Optional.ofNullable(pts.getRemovedAt()).isPresent())
				.collect(Collectors.toList());
		Map<Symbol, List<PortfolioToSymbol>> symbolToPtsMap = myPortfolioChanges.stream()
				.collect(Collectors.groupingBy(PortfolioToSymbol::getSymbol,
						Collectors.flatMapping((PortfolioToSymbol pts) -> Stream.of(pts), Collectors.toList())));
		symbolToPtsMap.entrySet().stream().map(entry -> {
			entry.setValue(entry.getValue().stream()
					.sorted((a, b) -> Optional.ofNullable(a.getChangedAt()).orElse(a.getRemovedAt())
							.compareTo(Optional.ofNullable(b.getChangedAt()).orElse(b.getRemovedAt())))
					.collect(Collectors.toList()));
			return entry;
		}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		Map<LocalDate, List<PortfolioToSymbol>> portfolioChangesMap = myPortfolioChanges.stream()
				.map(pts -> new PtsChangesByDay(Optional.ofNullable(pts.getChangedAt()).orElse(pts.getRemovedAt()), pts,
						findPreviousPts(symbolToPtsMap, pts)))
				.collect(Collectors.groupingBy(PtsChangesByDay::day,
						Collectors.flatMapping(pts -> Stream.of(pts.ptsChange), Collectors.toList())));
		SortedMap<LocalDate, List<PortfolioToSymbol>> sortedPortfolioChangesMap = ImmutableSortedMap
				.copyOf(portfolioChangesMap, (date1, date2) -> date1.compareTo(date2));
		final AtomicReference<QuoteDtoAndWeight> atomicWeight = new AtomicReference<>(
				new QuoteDtoAndWeight(BigDecimal.valueOf(-1L), new QuoteDto()));
		return dailyQuotes.stream()
				.map(myDailyQuote -> this.calcQuote(myDailyQuote.getLocalDay(), sortedPortfolioChangesMap, myDailyQuote,
						atomicWeight))
				.filter(myDto -> 0 < BigDecimal.ZERO.compareTo(myDto.getClose())).collect(Collectors.toList());
	}

	private Optional<PortfolioToSymbol> findPreviousPts(Map<Symbol, List<PortfolioToSymbol>> symbolToPtsMap,
			PortfolioToSymbol pts) {
		List<PortfolioToSymbol> listCopy = List.copyOf(symbolToPtsMap.get(pts.getSymbol()));
		Collections.reverse(listCopy); //needs check
		return listCopy.stream().filter(myPts -> Optional.ofNullable(myPts.getChangedAt()).orElse(myPts.getRemovedAt())
				.isBefore(Optional.ofNullable(pts.getChangedAt()).orElse(pts.getRemovedAt()))).findFirst();
	}

	private QuoteDto calcQuote(LocalDate day, SortedMap<LocalDate, List<PortfolioToSymbol>> portfolioChangesMap,
			DailyQuote dailyQuote, final AtomicReference<QuoteDtoAndWeight> atomicWeight) {
		if (dailyQuote.getLocalDay().isBefore(portfolioChangesMap.firstKey())) {
			throw new RuntimeException(String.format("No quotes for first portfolioChange: %s with Index: %s",
					portfolioChangesMap.firstKey().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE),
					dailyQuote.getSymbol()));
		}
		QuoteDto result = portfolioChangesMap.get(day).stream().map(pts -> this.createQuote(day, pts, dailyQuote))
				.reduce(new QuoteDto(null, null, null, BigDecimal.ZERO, 0L, day.atStartOfDay(), ""), (acc, value) -> {
					acc.setSymbol(value.getSymbol());
					acc.setClose(acc.getClose().add(value.getClose()));
					return acc;
				});
		return null;
	}

	private QuoteDto createQuote(LocalDate day, PortfolioToSymbol portfolioToSymbol, DailyQuote dailyQuote) {
		return new QuoteDto(null, null, null,
				this.currencyService.getCurrencyQuote(portfolioToSymbol, dailyQuote)
						.orElse(new Currency(null, null, null, null, null, null, BigDecimal.ZERO)).getClose()
						.multiply(BigDecimal.valueOf(portfolioToSymbol.getWeight())).multiply(dailyQuote.getClose()),
				0L, day.atStartOfDay(), portfolioToSymbol.getSymbol().getSymbol());
	}

	public List<QuoteDto> calculateIndexComparison(Long portfolioId, ComparisonIndex comparisonIndex, LocalDate from,
			LocalDate to) {
		return List.of();
	}

//	public Flux<QuoteDto> calculateIndexComparison(Long portfolioId, ComparisonIndex comparisonIndex) {
//		LOGGER.info("CalculateComparison Index: {} for PortfolioId: {}", comparisonIndex.getName(), portfolioId);
//		return this.currencyRepository.findAll().collectMultimap(entity -> entity.getLocalDay(), entity -> entity)
//				.flatMap(currencyMap -> this.portfolioToSymbolRepository.findByPortfolioId(portfolioId).collectList()
//						.flatMap(ptsEntities -> this.symbolRepository
//								.findAllById(ptsEntities.stream().map(ptsEntity -> ptsEntity.getSymbolId()).distinct()
//										.collect(Collectors.toList()))
//								.flatMap(symbolEntity -> this.dailyQuoteRepository
//										.findBySymbol(symbolEntity.getSymbol()).collectList()
//										.flatMap(dailyQuoteEntities -> this.calculateIndexes(ptsEntities, symbolEntity,
//												dailyQuoteEntities, currencyMap, comparisonIndex).collectList()))
//								.collectList()))
//				.flatMapMany(Flux::fromIterable).flatMap(Flux::fromIterable)
//				.collectMultimap(entity -> entity.getLocalDay(), entity -> entity).flatMap(entityMap -> {
//					LOGGER.info("List filer called. Size: {}", entityMap.size());
//					final DailyQuote myDailyQuote = new DailyQuote();
//					myDailyQuote.setClose(BigDecimal.ZERO);
//					List<DailyQuote> sumedEntities = entityMap.entrySet().stream()
//							.sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
//							.flatMap(entries -> Stream
//									.of(entries.getValue().stream().reduce(myDailyQuote, (oldQuote, newQuote) -> {
////										LOGGER.info("old close: {}, new close: {}", newQuote.getClose(),
////												oldQuote.getClose());
//										newQuote.setClose(newQuote.getClose().add(oldQuote.getClose()));
//										return newQuote;
//									})))
//							.collect(Collectors.toList());
//					return Mono.just(sumedE)ntities);
//				})
////				.collectList().flatMap(entities -> {
////					LOGGER.info("List filer called. Size: {}", entities.size());
////					return Mono.just(entities.stream().filter(entity -> entity.getLocalDay() != null)
////							.filter(ServiceUtils.distinctByKey(entity -> entity.getLocalDay().toString()))
////							.collect(Collectors.toList()));
////				})
//				.flux().flatMap(Flux::fromIterable).flatMap(entity -> this.mapToDto(entity));
//	}
//
//	public Flux<QuoteDto> calculateIndexComparison(Long portfolioId, ComparisonIndex comparisonIndex, LocalDate from,
//			LocalDate to) {
//		return this.calculateIndexComparison(portfolioId, comparisonIndex)
//				.filter(quoteDto -> quoteDto.getClose() != null && 0 < quoteDto.getClose().compareTo(BigDecimal.ZERO))
//				.filter(quoteDto -> -1 < quoteDto.getTimestamp().compareTo(from.atStartOfDay())
//						&& 1 > quoteDto.getTimestamp().compareTo(LocalDateTime.from(to.atTime(23, 59))));
//	}
//
//	private Flux<QuoteDto> mapToDto(DailyQuote entity) {
//		if (entity.getClose() != null && entity.getClose().compareTo(BigDecimal.valueOf(0.01)) > 0) {
//			LOGGER.info(entity.toString());
//		}
//		QuoteDto dto = new QuoteDto();
//		dto.setClose(entity.getClose());
//		dto.setSymbol(entity.getSymbol());
//		dto.setTimestamp(entity.getLocalDay().atStartOfDay());
//		dto.setVolume(entity.getVolume());
//		return Flux.fromIterable(List.of(dto));
//	}
//
//	private Flux<DailyQuote> upsertIndexComparisonDate(
//			Tuple3<LocalDate, AtomicReference<BigDecimal>, BigDecimal> tuple,
//			Map<LocalDate, DailyQuote> comparisonIndexQuoteMap,
//			Map<String, DailyQuote> currentSymbolQuoteMap,
//			Map<LocalDate, Collection<Currency>> currencyMap) {
//		Optional<DailyQuote> currentQuoteOpt = Optional
//				.ofNullable(currentSymbolQuoteMap.get(tuple.getA().toString()))
//				.filter(symbolQuote -> symbolQuote.getClose() != null && symbolQuote.getCurrencyId() != null);
//		Optional<DailyQuote> comparisonQuoteOpt = Optional.ofNullable(comparisonIndexQuoteMap.get(tuple.getA()))
//				.filter(indexQuote -> indexQuote.getClose() != null && indexQuote.getCurrencyId() != null);
//		if (comparisonQuoteOpt.isEmpty()) {
//			return Flux.empty();
//		}
//		SymbolCurrency symbolCurrency = List.of(ComparisonIndex.values()).stream()
//				.filter(compIndex -> compIndex.getSymbol().equals(comparisonQuoteOpt.get().getSymbol())).findFirst()
//				.map(compIndex -> compIndex.getCurrency()).orElseThrow();
//		Optional<Currency> currencyQuoteOpt = currencyMap.get(tuple.getA()) == null ? Optional.empty()
//				: currencyMap.get(tuple.getA()).stream()
//						.filter(currencyEntity -> symbolCurrency.toString().equals(currencyEntity.getTo_curr()))
//						.findFirst();
//		if (!symbolCurrency.equals(SymbolCurrency.EUR) && currencyQuoteOpt.isEmpty()) {
//			return Flux.empty();
//		}
//		BigDecimal comparisonQuoteEur = comparisonQuoteOpt.get().getClose().multiply(currencyQuoteOpt.get().getClose());
//		BigDecimal newPortfolioShares = tuple.getB().get()
//				.add(tuple.getC().compareTo(BigDecimal.valueOf(0.0001)) > 0
//						? tuple.getC().divide(comparisonQuoteEur, 10, RoundingMode.HALF_UP)
//						: BigDecimal.ZERO);
//		tuple.getB().set(newPortfolioShares);
//		DailyQuote newDailyQuoteEntity = currentQuoteOpt.orElse(new DailyQuote());
//		newDailyQuoteEntity.setCurrencyId(currencyQuoteOpt.get().getId());
//		newDailyQuoteEntity.setClose(newPortfolioShares.multiply(comparisonQuoteOpt.get().getClose()));
//		newDailyQuoteEntity.setLocalDay(tuple.getA());
//		newDailyQuoteEntity.setSymbol(comparisonQuoteOpt.get().getSymbol());
//		newDailyQuoteEntity.setSymbolId(comparisonQuoteOpt.get().getSymbolId());
//		newDailyQuoteEntity.setVolume(comparisonQuoteOpt.get().getVolume());
//
//		return Flux.just(newDailyQuoteEntity);
//	}
//
//	private Flux<DailyQuote> calculateIndexes(List<PortfolioToSymbol> ptsEntities,
//			Symbol symbolEntity, List<DailyQuote> dailyQuoteEntities,
//			Map<LocalDate, Collection<Currency>> currencyMap, ComparisonIndex comparisonIndex) {
//		final Map<String, DailyQuote> symbolQuoteMap = dailyQuoteEntities.stream()
//				.collect(Collectors.toMap(entity -> entity.getLocalDay().toString(), entity -> entity));
//		final List<Tuple3<PortfolioToSymbol, Symbol, DailyQuote>> sortedPortfolioChanges = this
//				.calculateSortedPortfolioChanges(ptsEntities, symbolEntity, dailyQuoteEntities);
//		Mono<Map<LocalDate, DailyQuote>> comparisonIndexQuotes = this.dailyQuoteRepository
//				.findBySymbol(comparisonIndex.getSymbol()).collectMap(entity -> entity.getLocalDay());
//		return comparisonIndexQuotes.flatMapMany(quoteMap -> this.recalculateComparisonIndex(quoteMap, symbolQuoteMap,
//				sortedPortfolioChanges, currencyMap, symbolEntity));
//	}
//
//	private Flux<DailyQuote> recalculateComparisonIndex(Map<LocalDate, DailyQuote> comparisonIndexQuoteMap,
//			Map<String, DailyQuote> currentSymbolQuoteMap,
//			List<Tuple3<PortfolioToSymbol, Symbol, DailyQuote>> sortedPortfolioChanges,
//			Map<LocalDate, Collection<Currency>> currencyMap, Symbol symbolEntity) {
//		final List<LocalDate> comparisonIndexDates = comparisonIndexQuoteMap.keySet().stream().sorted()
//				.collect(Collectors.toList());
//		final AtomicReference<BigDecimal> portfolioShares = new AtomicReference<BigDecimal>(BigDecimal.ZERO);
//		Flux<DailyQuote> result = Flux.mergeSequential(comparisonIndexDates.stream().flatMap(myDate -> {
////			LOGGER.info("Date: {}", myDate.toString());
//			BigDecimal difference = this.updatedPortfolioValue(myDate, currencyMap, sortedPortfolioChanges,
//					symbolEntity);
//			if (BigDecimal.ONE.compareTo(difference) <= 0) {
//				LOGGER.info("Date: {}, difference: {}, symbol: {}", myDate.toString(), difference.toString(),
//						symbolEntity.getSymbol());
//			}
//			return Stream.of(this.upsertIndexComparisonDate(
//					new Tuple3<LocalDate, AtomicReference<BigDecimal>, BigDecimal>(myDate, portfolioShares, difference),
//					comparisonIndexQuoteMap, currentSymbolQuoteMap, currencyMap));
//		}).collect(Collectors.toList()));
//		return result;
//	}
//
//	private BigDecimal updatedPortfolioValue(LocalDate currentDate,
//			Map<LocalDate, Collection<Currency>> currencyMap,
//			List<Tuple3<PortfolioToSymbol, Symbol, DailyQuote>> portfolioChanges,
//			Symbol symbolEntity) {
////		LOGGER.info("CurrentDate {}, ChangeDate {}", currentDate.toString(),
////				portfolioChanges.stream()
////						.filter(tuple3 -> !symbolEntity.getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER)
////								&& tuple3.getB().getSymbol().equals(symbolEntity.getSymbol()))
////						.anyMatch(tuple3 -> currentDate.isEqual(tuple3.getA().getChangedAt())));
//		BigDecimal difference = portfolioChanges.stream()
//				.filter(tuple3 -> !symbolEntity.getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER)
//						&& tuple3.getB().getSymbol().equals(symbolEntity.getSymbol()))
//				.filter(tuple3 -> (tuple3.getA().getChangedAt() != null
//						&& currentDate.isEqual(tuple3.getA().getChangedAt()))
//						|| (tuple3.getA().getRemovedAt() != null && currentDate.isEqual(tuple3.getA().getRemovedAt())))
//				.reduce(BigDecimal.ZERO, (value, tuple3) -> {
//					BigDecimal newValue = BigDecimal.ZERO;
//					if (SymbolCurrency.EUR.toString().equals(tuple3.getB().getCurr())) {
//						newValue = BigDecimal.valueOf(tuple3.getA().getWeight()).multiply(tuple3.getC().getClose());
//					} else if (currencyMap.containsKey(currentDate) && currencyMap.get(currentDate).stream()
//							.anyMatch(currencyEntity -> tuple3.getB().getCurr().equals(currencyEntity.getTo_curr()))) {
//						BigDecimal currencyValue = currencyMap.get(currentDate).stream()
//								.filter(currencyEntity -> tuple3.getB().getCurr().equals(currencyEntity.getTo_curr()))
//								.map(currencyEntity -> currencyEntity.getClose()).findFirst().orElseThrow();
//						newValue = BigDecimal.valueOf(tuple3.getA().getWeight())
//								.multiply(tuple3.getC().getClose().multiply(currencyValue));
//					}
////					LOGGER.info("NewValue: {}", newValue.toString());
//					return value.add(newValue);
//				}, BigDecimal::add);
//		return difference;
//	}
//
//	private List<Tuple3<PortfolioToSymbol, Symbol, DailyQuote>> calculateSortedPortfolioChanges(
//			List<PortfolioToSymbol> ptsEntities, Symbol symbolEntity,
//			List<DailyQuote> dailyQuoteEntities) {
//		Map<LocalDate, PortfolioToSymbol> myPtsEntities = ptsEntities.stream()
//				.filter(ptsEntity -> ptsEntity.getSymbolId().equals(symbolEntity.getId()))
//				.collect(Collectors.toMap(ptsEntity -> ptsEntity.getChangedAt() != null ? ptsEntity.getChangedAt()
//						: ptsEntity.getRemovedAt(), ptsEntity -> ptsEntity));
//		Map<LocalDate, DailyQuote> myDailyQuoteEntities = dailyQuoteEntities.stream()
//				.filter(dailyQuoteEntity -> myPtsEntities.containsKey(dailyQuoteEntity.getLocalDay()))
//				.collect(Collectors.toMap(dailyQuoteEntity -> dailyQuoteEntity.getLocalDay(),
//						dailyQuoteEntity -> dailyQuoteEntity));
//		List<Tuple3<PortfolioToSymbol, Symbol, DailyQuote>> portfolioChanges = myPtsEntities.keySet()
//				.stream().sorted()
//				.flatMap(myDate -> Stream.of(new Tuple3<PortfolioToSymbol, Symbol, DailyQuote>(
//						myPtsEntities.get(myDate), symbolEntity, myDailyQuoteEntities.get(myDate))))
//				.collect(Collectors.toList());
//		return portfolioChanges;
//	}
}
