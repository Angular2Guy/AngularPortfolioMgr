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
import java.time.LocalDateTime;
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

import ch.xxx.manager.dto.PortfolioDto;
import ch.xxx.manager.dto.SymbolDto;
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
public class PortfolioService {
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

	public Flux<PortfolioDto> getPortfoliosByUserId(Long userId) {
		return this.portfolioRepository.findByUserId(userId).flatMapSequential(entity -> this.convertFlux(entity));
	}

	public Mono<PortfolioDto> getPortfolioById(Long portfolioId) {
		return this.portfolioRepository.findById(portfolioId).flatMap(entity -> this.convert(entity));
	}

	public Mono<PortfolioDto> addPortfolio(PortfolioDto dto) {
		return this.portfolioRepository.save(this.convert(dto))
				.flatMap(myEntity -> this.convertFlux(myEntity).singleOrEmpty());
	}

	public Mono<Boolean> addSymbolToPortfolio(PortfolioDto dto, Long symbolId, Long weight, LocalDateTime changedAt) {
		return this.portfolioToSymbolRepository
				.save(this.createPtsEntity(dto, symbolId, weight, changedAt.toLocalDate()))
				.flatMap(myEntity -> this.calculatePortfolio(myEntity.getId()));
	}

	public Mono<Boolean> updatePortfolioSymbolWeight(PortfolioDto dto, Long symbolId, Long weight,
			LocalDateTime changedAt) {
		return this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(dto.getId(), symbolId)
				.flatMap(myEntity -> Flux.just(
						this.updatePtsEntity(myEntity, Optional.of(weight), changedAt.toLocalDate(), Optional.empty())))
				.flatMap(newEntity -> Flux.just(this.portfolioToSymbolRepository.save(newEntity))).count()
				.flatMap(num -> this.calculatePortfolio(dto.getId()));
	}

	public Mono<Boolean> removeSymbolFromPortfolio(Long portfolioId, Long symbolId, LocalDateTime removedAt) {
		return this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)
				.flatMap(entity -> Flux.just(this.portfolioToSymbolRepository.save(this.updatePtsEntity(entity,
						Optional.empty(), LocalDate.now(), Optional.of(removedAt.toLocalDate())))))
				.count().flatMap(num -> this.calculatePortfolio(portfolioId));
	}

	private PortfolioToSymbolEntity updatePtsEntity(PortfolioToSymbolEntity entity, Optional<Long> weightOpt,
			LocalDate changedAt, Optional<LocalDate> removedAtOpt) {
		weightOpt.ifPresent(weight -> entity.setWeight(weight));
		removedAtOpt.ifPresentOrElse(date -> entity.setRemovedAt(date), () -> entity.setChangedAt(changedAt));
		return entity;
	}

	private PortfolioToSymbolEntity createPtsEntity(PortfolioDto dto, Long symbolId, Long weight, LocalDate changedAt) {
		PortfolioToSymbolEntity entity = new PortfolioToSymbolEntity();
		entity.setPortfolioId(dto.getId());
		entity.setSymbolId(symbolId);
		entity.setWeight(weight);
		entity.setChangedAt(changedAt);
		return entity;
	}

	private PortfolioEntity convert(PortfolioDto dto) {
		PortfolioEntity entity = new PortfolioEntity();
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setUserId(dto.getUserId());
		entity.setCreatedAt(dto.getCreatedAt() == null ? LocalDate.now() : dto.getCreatedAt().toLocalDate());
		return entity;
	}

	private Mono<PortfolioDto> updatePortfolioDto(PortfolioToSymbolEntity entity, SymbolEntity symbolEntity,
			PortfolioDto dto) {
		SymbolDto symbolDto = new SymbolDto(symbolEntity.getId(), symbolEntity.getSymbol(), symbolEntity.getName(),
				entity.getChangedAt(), entity.getRemovedAt());
		symbolDto.setWeight(entity.getWeight());
		dto.getSymbols().add(symbolDto);
		return Mono.just(dto);
	}

	private Mono<PortfolioDto> convert(PortfolioToSymbolEntity entity, PortfolioDto dto) {
		if (entity.getId() == null) {
			return Mono.just(dto);
		}
		return this.symbolRepository.findById(entity.getSymbolId())
				.flatMap(symbolEntity -> updatePortfolioDto(entity, symbolEntity, dto));
	}

	private Mono<PortfolioDto> convert(PortfolioEntity entity) {
		final PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName(),
				entity.getCreatedAt().atStartOfDay(), entity.getMonth1(), entity.getMonths6(), entity.getYear1(),
				entity.getYear2(), entity.getYear5(), entity.getYear10());
		return this.portfolioToSymbolRepository.findByPortfolioId(dto.getId())
				.switchIfEmpty(Mono.just(new PortfolioToSymbolEntity()))
				.flatMapSequential(p2SymbolEntity -> this.convert(p2SymbolEntity, dto)).distinct().single();
	}

	private Flux<PortfolioDto> convertFlux(PortfolioEntity entity) {
		final PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName(),
				entity.getCreatedAt().atStartOfDay(), entity.getMonth1(), entity.getMonths6(), entity.getYear1(),
				entity.getYear2(), entity.getYear5(), entity.getYear10());
		return this.portfolioToSymbolRepository.findByPortfolioId(dto.getId())
				.switchIfEmpty(Flux.just(new PortfolioToSymbolEntity()))
				.flatMapSequential(p2SymbolEntity -> this.convert(p2SymbolEntity, dto)).distinct();
	}

	private Mono<Boolean> calculatePortfolio(Long portfolioId) {
		Mono<List<DailyQuoteEntity>> portfolioQuotes = Mono
				.zip(this.portfolioToSymbolRepository.findByPortfolioId(portfolioId)
						.collectMap(myEntity -> myEntity.getSymbolId(), myEntity -> myEntity),
						this.symbolRepository.findByPortfolioId(portfolioId)
								.collectMap(localEntity -> localEntity.getId(), localEntity -> localEntity))
				.flatMap(data -> Mono.just(new Tuple<>(data.getT1(), data.getT2())))
				.flatMap(tuple -> createMultiMap(tuple)).flatMap(myTuple -> this.dailyQuoteRepository
						.saveAll(this.updatePortfolioSymbol(myTuple)).collectList());
		return this.portfolioRepository.findById(portfolioId).flatMap(portfolio -> this.updatePortfolio(portfolio, portfolioQuotes))
				.flatMap(portfolio -> this.portfolioRepository.save(portfolio)).flatMap(portfolio -> Mono.just(portfolio.getId() != null));
//		return Mono.just(Boolean.TRUE);
	}

	private Mono<PortfolioEntity> updatePortfolio(PortfolioEntity entity,
			Mono<List<DailyQuoteEntity>> portfolioQuotesRef) {
		// Now all the portfolioQuotes are needed for the calculation!!!
		List<DailyQuoteEntity> portfolioQuotes = portfolioQuotesRef.block();
		entity.setMonth1(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(1, ChronoUnit.MONTHS)));
		entity.setMonths6(this.calcPortfolioValueAtDate(portfolioQuotes, LocalDate.now().minus(6, ChronoUnit.MONTHS)));
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
