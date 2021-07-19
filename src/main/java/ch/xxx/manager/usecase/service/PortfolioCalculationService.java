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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.model.entity.CurrencyRepository;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioRepository;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbolRepository;
import reactor.core.publisher.Mono;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class PortfolioCalculationService {
	private static final Logger LOG = LoggerFactory.getLogger(PortfolioCalculationService.class);
	private final PortfolioRepository portfolioRepository;
	private final DailyQuoteRepository dailyQuoteRepository;
	private final CurrencyRepository currencyRepository;
	private final PortfolioToSymbolRepository portfolioAndSymbolRepository;

	public PortfolioCalculationService(PortfolioRepository portfolioRepository,
			DailyQuoteRepository dailyQuoteRepository, CurrencyRepository currencyRepository,
			PortfolioToSymbolRepository portfolioAndSymbolRepository) {
		this.portfolioRepository = portfolioRepository;
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.currencyRepository = currencyRepository;
		this.portfolioAndSymbolRepository = portfolioAndSymbolRepository;
	}

	public Portfolio calculatePortfolio(Long portfolioId) {
		List<PortfolioToSymbol> portfolioToSymbols = this.portfolioAndSymbolRepository.findPortfolioCalcEntitiesByPortfolioId(portfolioId);
		
		return null;
//		List<DailyQuote> portfolioQuotes = Mono.zip(
//				this.portfolioAndSymbolRepository.findPortfolioCalcEntitiesByPortfolioId(portfolioId)
//						.collectMap(myEntity -> myEntity.getSymbolId(), myEntity -> myEntity),
//				this.currencyRepository.findAll().collectMultimap(entity -> entity.getLocalDay(), entity -> entity))
//				.flatMap(data -> Mono.just(new Tuple<>(data.getT1(), data.getT2())))
//				.flatMap(tuple -> createMultiMap(tuple)).flatMap(myTuple -> this.updatePortfolioQuotes(myTuple));
//		return this.portfolioRepository.findById(portfolioId)
//				.flatMap(portfolio -> this.updatePortfolio(portfolio, portfolioQuotes))
//				.flatMap(portfolio -> this.portfolioRepository.save(portfolio));
	}

//	private Mono<List<DailyQuote>> updatePortfolioQuotes(
//			Tuple3<Map<Long, PortfolioAndSymbol>, Map<Long, Collection<DailyQuote>>, Map<LocalDate, Collection<Currency>>> myTuple) {
//		Optional<PortfolioAndSymbol> pAndSymEntityOpt = myTuple.getA().values().stream()
//				.filter(symbolEntity -> symbolEntity.getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER)).findFirst();
//		Collection<DailyQuote> quotesToDelete = List.of();
//		if (pAndSymEntityOpt.isPresent()) {
//			Optional<Entry<Long, Collection<DailyQuote>>> entryOpt = myTuple.getB().entrySet().stream()
//					.filter(entry -> entry.getKey().equals(pAndSymEntityOpt.get().getSymbolId())).findFirst();
//			quotesToDelete = entryOpt.isEmpty() ? quotesToDelete : entryOpt.get().getValue();
//		}
//		return this.dailyQuoteRepository.deleteAll(quotesToDelete)
//				.then(this.dailyQuoteRepository.saveAll(this.updatePortfolioSymbol(myTuple)).collectList());
//	}
//
//	private Mono<Portfolio> updatePortfolio(Portfolio entity,
//			Mono<List<DailyQuote>> portfolioQuotesRef) {
//		// Now all the portfolioQuotes are needed for the calculation!!!
//		List<DailyQuote> portfolioQuotes = portfolioQuotesRef.block();
//		entity.setMonth1(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(1, ChronoUnit.MONTHS)));
//		entity.setMonth6(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(6, ChronoUnit.MONTHS)));
//		entity.setYear1(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(1, ChronoUnit.YEARS)));
//		entity.setYear2(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(2, ChronoUnit.YEARS)));
//		entity.setYear5(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(5, ChronoUnit.YEARS)));
//		entity.setYear10(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(10, ChronoUnit.YEARS)));
//		return Mono.just(entity);
//	}
//
//	private BigDecimal calcPortfolioValueAtDate(List<DailyQuote> portfolioQuotes, LocalDate dateAt) {
//		return portfolioQuotes.stream()
//				.sorted(Comparator.comparing(DailyQuote::getLocalDay, (a, b) -> a.compareTo(b)).reversed())
//				.filter(quote -> quote.getLocalDay().isBefore(dateAt)).flatMap(quote -> Stream.of(quote.getClose()))
//				.findFirst().orElse(BigDecimal.ZERO);
//	}
//
//	private List<DailyQuote> updatePortfolioSymbol(
//			Tuple3<Map<Long, PortfolioAndSymbol>, Map<Long, Collection<DailyQuote>>, Map<LocalDate, Collection<Currency>>> tuple3) {
//		final Set<LocalDate> blockedDates = new HashSet<>();
//		Optional<PortfolioAndSymbol> pAndSymEntityOpt = tuple3.getA().values().stream()
//				.filter(symbolEntity -> symbolEntity.getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER)).findFirst();
//		Optional<List<Tuple3<Long, LocalDate, BigDecimal>>> reduceOpt = tuple3.getA().entrySet().stream()
//				.filter(value -> pAndSymEntityOpt.isEmpty()
//						|| !pAndSymEntityOpt.get().getSymbolId().equals(value.getValue().getSymbolId()))
//				.flatMap(
//						value -> Stream.of(new Tuple<Long, PortfolioAndSymbol>(value.getKey(), value.getValue())))
//				.flatMap(tuple -> Stream.of(new Tuple<PortfolioAndSymbol, Collection<DailyQuote>>(
//						tuple.getB(), tuple3.getB().get(tuple.getB().getSymbolId()))))
//				.flatMap(quotesTuple -> Stream.of(quotesTuple.getB().stream()
//						.filter(ServiceUtils.distinctByKey(myQuote -> "" + myQuote.getLocalDay()))
//						.filter(quote -> quote.getLocalDay().compareTo(quotesTuple.getA().getChangedAt()) > -1
//								&& (quotesTuple.getA().getRemovedAt() == null
//										|| quote.getLocalDay().isBefore(quotesTuple.getA().getRemovedAt())))
//						.filter(myQuote -> {
//							if (myQuote.getClose() == null || quotesTuple.getA().getWeight() == null
//									|| this.findCurrencyByDateAndQuote(myQuote, tuple3.getC(), myQuote.getLocalDay(),
//											tuple3.getA()).isEmpty()) {
//								blockedDates.add(myQuote.getLocalDay());
//							}
//							return true;
//						}).filter(myQuote -> blockedDates.contains(myQuote.getLocalDay()))
//						.flatMap(quote -> Stream
//								.of(new Tuple3<Long, LocalDate, BigDecimal>(quote.getSymbolId(), quote.getLocalDay(),
//										this.calculatePortfolioQuote(quote.getClose(), quotesTuple.getA().getWeight(),
//												this.findCurrencyByDateAndQuote(quote, tuple3.getC(),
//														quote.getLocalDay(), tuple3.getA())))))
//						.collect(Collectors.toList())))
//				.reduce((oldList, newList) -> {
////						LOG.info("oldList: " + oldList.stream().flatMap(myTuple3 -> Stream.of(myTuple3.getA())).distinct()
////								.flatMap(myId -> Stream.of(" " + myId)).collect(Collectors.toList()));
////						LOG.info("newList: " + newList.stream().flatMap(myTuple3 -> Stream.of(myTuple3.getA())).distinct()
////								.flatMap(myId -> Stream.of(" " + myId)).collect(Collectors.toList()));
////						LOG.info("oldList: " + oldList.stream().collect(Collectors.groupingBy(myTuple3 -> myTuple3.getB()))
////								.entrySet().stream().flatMap(entry -> Stream.of("" + entry.getKey() + " "
////										+ entry.getValue().size() + " " + entry.getValue().get(0).getA()))
////								.collect(Collectors.toList()));
////						LOG.info("newList: " + newList.stream().collect(Collectors.groupingBy(myTuple3 -> myTuple3.getB()))
////								.entrySet().stream().flatMap(entry -> Stream.of("" + entry.getKey() + " "
////										+ entry.getValue().size() + " " + entry.getValue().get(0).getA()))
////								.collect(Collectors.toList()));					
//					oldList = oldList.stream()
//							.filter(tuple3Old -> newList.stream()
//									.anyMatch(tuple3New -> tuple3New.getB().isEqual(tuple3Old.getB())))
//							.collect(Collectors.toList());
//					List<Tuple3<Long, LocalDate, BigDecimal>> resultList = Stream
//							.concat(oldList.stream(), newList.stream())
//							.filter(ServiceUtils.distinctByKey(myTuple3 -> "" + myTuple3.getA() + myTuple3.getB()))
//							.collect(Collectors.toList());
//					return resultList;
//				});
//		List<Tuple3<Long, LocalDate, BigDecimal>> portfolioTuples = reduceOpt.orElse(List.of());
//		String randomString = ServiceUtils.generateRandomPortfolioSymbol();
//
//		List<DailyQuote> portfolioQuotes = portfolioTuples.stream()
//				.collect(Collectors.groupingBy(tuple -> tuple.getB())).entrySet().stream().flatMap(entry -> {
////						LOG.info("Size: " + entry.getValue().size());
////						entry.getValue().forEach(myTuple3 -> {
////							logQuoteTuple(myTuple3);
////						});
//					return Stream.of(entry.getValue().stream()
//							.reduce(new Tuple3<Long, LocalDate, BigDecimal>(entry.getValue().get(0).getA(),
//									entry.getValue().get(0).getB(), BigDecimal.ZERO),
//									(t1, t2) -> new Tuple3<Long, LocalDate, BigDecimal>(t1.getA(), t1.getB(),
//											t1.getC().add(t2.getC()))));
//				}).collect(Collectors.toList()).stream()
//				.flatMap(localTuple -> Stream.of(this.createQuote(localTuple, tuple3.getA(), randomString)))
//				.collect(Collectors.toList());
//		portfolioQuotes.stream().map(myQuote -> myQuote.getSymbolId()).distinct().collect(Collectors.toList())
//				.forEach(myEntity -> LOG.info("Symbol: {}", myEntity));
//		return portfolioQuotes;
//	}
//
//	private void logQuoteTuple(Tuple3<Long, LocalDate, BigDecimal> myTuple3) {
//		LOG.info("---");
//		LOG.info(myTuple3.getC().toPlainString());
//		LOG.info(myTuple3.getB().toString());
//		LOG.info(myTuple3.getA().toString());
//		LOG.info("---");
//	}
//
//	private BigDecimal calculatePortfolioQuote(BigDecimal close, Long weight, Optional<Currency> currencyOpt) {
//		BigDecimal currencyValue = currencyOpt.isEmpty() ? BigDecimal.ONE : currencyOpt.get().getClose();
//		BigDecimal symbolWeight = weight == null || weight.longValue() < 1 ? BigDecimal.ONE
//				: BigDecimal.valueOf(weight);
//		BigDecimal result = close.multiply(symbolWeight).divide(currencyValue, 4, RoundingMode.HALF_UP);
////		LOG.info("---");
////		LOG.info(close.toString());
////		LOG.info(currencyValue.toString());
////		LOG.info(symbolWeight.toString());
////		LOG.info(result.toString());
////		LOG.info(currencyOpt.isPresent() ? currencyOpt.get().getLocalDay().toString() : "NA");
////		LOG.info("---");
//		return result;
//	}
//
//	private Optional<Currency> findCurrencyByDateAndQuote(DailyQuote quote,
//			Map<LocalDate, Collection<Currency>> currencyMap, LocalDate localDate,
//			Map<Long, PortfolioAndSymbol> pAndSMap) {
//		Map<Long, PortfolioAndSymbol> symbolToPAndSMap = pAndSMap.values().stream()
//				.collect(Collectors.toMap(pAndSEntity -> pAndSEntity.getSymbolId(), pAndSEntity -> pAndSEntity));
//		Optional<Currency> entityOpt = currencyMap.get(localDate) == null
//				|| pAndSMap.get(quote.getSymbolId()) == null
//						? Optional.empty()
//						: currencyMap.get(localDate).stream()
//								.filter(entity -> SymbolCurrency.valueOf(entity.getTo_curr()).equals(
//										SymbolCurrency.valueOf(symbolToPAndSMap.get(quote.getSymbolId()).getCurr())))
//								.findFirst();
////			entityOpt.ifPresent(entity -> LOG.info("Date: {} value: {}", entity.getLocalDay().toString(), entity.getClose()));
//		return entityOpt;
//	}
//
//	private DailyQuote createQuote(Tuple3<Long, LocalDate, BigDecimal> tuple3,
//			Map<Long, PortfolioAndSymbol> pAndSMap, String newSymbolStr) {
////		this.logQuoteTuple(tuple3);
//		Optional<PortfolioAndSymbol> symbolEntityOpt = pAndSMap.values().stream()
//				.filter(pAndSEntity -> pAndSEntity.getSymbol().contains(ServiceUtils.PORTFOLIO_MARKER)).findFirst();
//		DailyQuote entity = new DailyQuote();
//		entity.setClose(tuple3.getC());
//		entity.setLocalDay(tuple3.getB());
//		entity.setSymbolId(symbolEntityOpt.isPresent() ? symbolEntityOpt.get().getSymbolId() : null);
//		entity.setSymbol(symbolEntityOpt.isPresent() ? symbolEntityOpt.get().getSymbol() : newSymbolStr);
//		return entity;
//	}
//
//	private Mono<Tuple3<Map<Long, PortfolioAndSymbol>, Map<Long, Collection<DailyQuote>>, Map<LocalDate, Collection<Currency>>>> createMultiMap(
//			Tuple<Map<Long, PortfolioAndSymbol>, Map<LocalDate, Collection<Currency>>> tuple) {
//
//		Map<Long, Collection<DailyQuote>> quotesMap = Flux
//				.fromIterable(tuple.getA().values().stream()
//						.flatMap(pAndSEntity -> Stream.of(pAndSEntity.getSymbolId())).collect(Collectors.toList()))
//				.parallel().flatMap(symId -> this.dailyQuoteRepository.findBySymbolId(symId)
//						.collectMultimap(quote -> symId, quote -> quote))
//				.reduce((oldMap, newMap) -> {
//					oldMap.putAll(newMap);
//					return oldMap;
//				}).block();
//		return Mono.just(
//				new Tuple3<Map<Long, PortfolioAndSymbol>, Map<Long, Collection<DailyQuote>>, Map<LocalDate, Collection<Currency>>>(
//						tuple.getA(), quotesMap, tuple.getB()));
//	}
}
