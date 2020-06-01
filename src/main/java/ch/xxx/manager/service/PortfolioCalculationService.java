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
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.entity.DailyQuoteEntity;
import ch.xxx.manager.entity.PortfolioEntity;
import ch.xxx.manager.entity.PortfolioToSymbolEntity;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.jwt.Tuple;
import ch.xxx.manager.repository.DailyQuoteRepository;
import ch.xxx.manager.repository.PortfolioRepository;
import ch.xxx.manager.repository.PortfolioToSymbolRepository;
import ch.xxx.manager.repository.SymbolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class PortfolioCalculationService {
	private final static String PORTFOLIO_MARKER = "$#@";
	private final static int SYMBOL_LENGTH = 15;
	@Autowired
	private PortfolioRepository portfolioRepository;
	@Autowired
	private PortfolioToSymbolRepository portfolioToSymbolRepository;
	@Autowired
	private SymbolRepository symbolRepository;
	@Autowired
	private DailyQuoteRepository dailyQuoteRepository;
	
	public Mono<PortfolioEntity> calculatePortfolio(Long portfolioId) {
		Mono<List<DailyQuoteEntity>> portfolioQuotes = Mono
				.zip(this.portfolioToSymbolRepository.findByPortfolioId(portfolioId)
						.collectMap(myEntity -> myEntity.getSymbolId(), myEntity -> myEntity),
						this.symbolRepository.findByPortfolioId(portfolioId)
								.collectMap(localEntity -> localEntity.getId(), localEntity -> localEntity))
				.flatMap(data -> Mono.just(new Tuple<>(data.getT1(), data.getT2())))
				.flatMap(tuple -> createMultiMap(tuple)).flatMap(myTuple -> this.dailyQuoteRepository
						.saveAll(this.updatePortfolioSymbol(myTuple)).collectList());
		return this.portfolioRepository.findById(portfolioId).flatMap(portfolio -> this.updatePortfolio(portfolio, portfolioQuotes))
				.flatMap(portfolio -> this.portfolioRepository.save(portfolio));
//		return Mono.just(Boolean.TRUE);
	}

	private Mono<PortfolioEntity> updatePortfolio(PortfolioEntity entity,
			Mono<List<DailyQuoteEntity>> portfolioQuotesRef) {
		// Now all the portfolioQuotes are needed for the calculation!!!
		List<DailyQuoteEntity> portfolioQuotes = portfolioQuotesRef.block();
		entity.setMonth1(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(1, ChronoUnit.MONTHS)));
		entity.setMonth6(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(6, ChronoUnit.MONTHS)));
		entity.setYear1(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(1, ChronoUnit.YEARS)));
		entity.setYear2(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(2, ChronoUnit.YEARS)));
		entity.setYear5(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(5, ChronoUnit.YEARS)));
		entity.setYear10(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(10, ChronoUnit.YEARS)));
		return Mono.just(entity);
	}

	private BigDecimal calcPortfolioValueAtDate(List<DailyQuoteEntity> portfolioQuotes, LocalDate dateAt) {
		return portfolioQuotes.stream()
				.sorted(Comparator.comparing(DailyQuoteEntity::getLocalDay, (a, b) -> a.compareTo(b)).reversed())
				.filter(quote -> quote.getLocalDay().isBefore(dateAt)).flatMap(quote -> Stream.of(quote.getClose()))
				.findFirst().orElse(BigDecimal.ZERO);
	}

	private List<DailyQuoteEntity> updatePortfolioSymbol(
			Tuple3<Map<Long, PortfolioToSymbolEntity>, Map<Long, SymbolEntity>, Map<Long, Collection<DailyQuoteEntity>>> tuple3) {
		Optional<SymbolEntity> symbolEntityOpt = tuple3.getB().values().stream()
				.filter(symbolEntity -> symbolEntity.getSymbol().contains(PORTFOLIO_MARKER)).findFirst();
		Optional<List<Tuple3<Long, LocalDate, BigDecimal>>> reduceOpt = tuple3.getA().entrySet().stream()
				.filter(value -> symbolEntityOpt.isEmpty() || !symbolEntityOpt.get().getId().equals(value.getKey()))
				.flatMap(value -> Stream.of(new Tuple<Long, PortfolioToSymbolEntity>(value.getKey(), value.getValue())))
				.flatMap(tuple -> Stream.of(new Tuple<Long, Collection<DailyQuoteEntity>>(tuple.getB().getWeight(),
						tuple3.getC().get(tuple.getA()))))
				.flatMap(
						quotesTuple -> Stream.of(quotesTuple.getB().stream()
								.flatMap(quote -> Stream.of(new Tuple3<Long, LocalDate, BigDecimal>(quote.getSymbolId(),
										quote.getLocalDay(),
										quote.getClose().multiply(BigDecimal.valueOf(quotesTuple.getA())))))
								.collect(Collectors.toList())))
				.reduce((oldList, newList) -> {
					oldList.addAll(newList);
					return oldList;
				});
		List<Tuple3<Long, LocalDate, BigDecimal>> portfolioTuples = reduceOpt.orElse(List.of());

		List<DailyQuoteEntity> portfolioQuotes = portfolioTuples.stream()
				.collect(Collectors.groupingBy(tuple -> tuple.getB())).entrySet().stream()
				.flatMap(entry -> Stream.of(entry.getValue().stream()
						.reduce(new Tuple3<Long, LocalDate, BigDecimal>(entry.getValue().get(0).getA(),
								entry.getValue().get(0).getB(), BigDecimal.ZERO),
								(t1, t2) -> new Tuple3<Long, LocalDate, BigDecimal>(t1.getA(), t1.getB(),
										t1.getC().add(t2.getC())))))
				.collect(Collectors.toList()).stream()
				.flatMap(localTuple -> Stream.of(this.createQuote(localTuple, tuple3.getB())))
				.collect(Collectors.toList());
		return portfolioQuotes;
	}

	private DailyQuoteEntity createQuote(Tuple3<Long, LocalDate, BigDecimal> tuple3,
			Map<Long, SymbolEntity> symbolsMap) {
		Optional<SymbolEntity> symbolEntityOpt = symbolsMap.values().stream()
				.filter(symbolEntity -> symbolEntity.getSymbol().contains(PORTFOLIO_MARKER)).findFirst();
		DailyQuoteEntity entity = new DailyQuoteEntity();
		entity.setClose(tuple3.getC());
		entity.setLocalDay(tuple3.getB());
		entity.setSymbolId(tuple3.getA());
		entity.setSymbol(symbolEntityOpt.isPresent() ? symbolEntityOpt.get().getSymbol()
				: ServiceUtils.generateRandomString(SYMBOL_LENGTH - PORTFOLIO_MARKER.length()));
		return entity;
	}

	private Mono<Tuple3<Map<Long, PortfolioToSymbolEntity>, Map<Long, SymbolEntity>, Map<Long, Collection<DailyQuoteEntity>>>> createMultiMap(
			Tuple<Map<Long, PortfolioToSymbolEntity>, Map<Long, SymbolEntity>> tuple) {
		Map<Long, Collection<DailyQuoteEntity>> quotesMap = Flux
				.fromIterable(tuple.getB().keySet()).parallel().flatMap(symId -> this.dailyQuoteRepository
						.findBySymbolId(symId).collectMultimap(quote -> symId, quote -> quote))
				.reduce((oldMap, newMap) -> {
					oldMap.putAll(newMap);
					return oldMap;
				}).block();
		return Mono.just(
				new Tuple3<Map<Long, PortfolioToSymbolEntity>, Map<Long, SymbolEntity>, Map<Long, Collection<DailyQuoteEntity>>>(
						tuple.getA(), tuple.getB(), quotesMap));
	}
}
