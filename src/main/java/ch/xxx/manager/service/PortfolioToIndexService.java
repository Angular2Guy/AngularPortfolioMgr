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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.entity.DailyQuoteEntity;
import ch.xxx.manager.entity.PortfolioToSymbolEntity;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.repository.DailyQuoteRepository;
import ch.xxx.manager.repository.PortfolioToSymbolRepository;
import ch.xxx.manager.repository.SymbolRepository;
import ch.xxx.manager.service.ServiceUtils.RefMarker;
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

	public Flux<Long> calculateIndexComparison(Long portfolioId) {
		final List<String> refMarkerStrs = List.of(ServiceUtils.RefMarker.values()).stream()
				.map(refMarker -> refMarker.getMarker()).collect(Collectors.toList());
		final List<RefMarker> refMarkers = List.of(ServiceUtils.RefMarker.values());
		return this.addIndexComparisonSymbols(portfolioId, refMarkers).flux().flatMap(result -> {
			LOGGER.info("ComparisonIndexes added: {}", result);
			return this.portfolioToSymbolRepository.findByPortfolioId(portfolioId).collectList()
					.flatMap(ptsEntities -> this.symbolRepository
							.findAllById(ptsEntities.stream().map(ptsEntity -> ptsEntity.getSymbolId()).distinct()
									.collect(Collectors.toList()))
							.filter(symbolEntity -> refMarkerStrs.stream()
									.anyMatch(refMarkerStr -> symbolEntity.getSymbol().contains(refMarkerStr)))
							.flatMap(
									symbolEntity -> this.dailyQuoteRepository.findBySymbol(symbolEntity.getSymbol())
											.collectList().flatMap(dailyQuoteEntities -> this
													.calculateIndexes(ptsEntities, symbolEntity, dailyQuoteEntities))
											.flux())
							.collectList());
		}).flatMap(results -> Flux.just(results.stream().count()));
	}

	private Mono<Long> upsertIndexComparisonSymbols(Long portfolioId, List<RefMarker> refMarkers) {
		this.portfolioToSymbolRepository.findByPortfolioId(portfolioId).collectList()
				.flatMap(ptsEntities -> this.symbolRepository
						.findAllById(ptsEntities.stream().map(ptsEntity -> ptsEntity.getSymbolId()).distinct()
								.collect(Collectors.toList()))
						.filter(symbolEntity -> refMarkers.stream()
								.anyMatch(refMarker -> symbolEntity.getSymbol().contains(refMarker.getMarker())))
						.collectList().flatMap(symbolEntities -> Mono.just(symbolEntities)));
		return Mono.empty();
	}

	private Mono<Long> addIndexComparisonSymbols(Long PortfolioId, List<RefMarker> refMarkers) {
		return Flux.fromStream(refMarkers.stream().flatMap(refMarker -> {
			SymbolEntity symbolEntity = new SymbolEntity();
			Stream<ComparisonIndexes> ciStream = List.of(ComparisonIndexes.values()).stream()
					.filter(comparisonIndex -> comparisonIndex.getRefMarker().equals(refMarker));
			symbolEntity.setCurr(ciStream.map(comparisonIndex -> comparisonIndex.getCurrency().toString()).findFirst()
					.orElseThrow());
			symbolEntity.setName(ciStream.map(comparisonIndex -> comparisonIndex.getName()).findFirst().orElseThrow());
			symbolEntity.setSource(
					ciStream.map(comparisonIndex -> comparisonIndex.getSource().toString()).findFirst().orElseThrow());
			symbolEntity
					.setSymbol(ciStream.map(comparisonIndex -> comparisonIndex.getSymbol()).findFirst().orElseThrow());
			return Stream.of(this.symbolRepository.save(symbolEntity).flatMap(newSymbolEntity -> {
				PortfolioToSymbolEntity ptsEntity = new PortfolioToSymbolEntity();
				ptsEntity.setPortfolioId(PortfolioId);
				ptsEntity.setChangedAt(LocalDate.now());
				ptsEntity.setWeight(1L);
				ptsEntity.setSymbolId(newSymbolEntity.getId());
				return this.portfolioToSymbolRepository.save(ptsEntity);
			}));
		})).collectList().flatMap(results -> Mono.just(Integer.valueOf(results.size()).longValue()));
	}

	private Mono<Long> calculateIndexes(List<PortfolioToSymbolEntity> ptsEntities, SymbolEntity symbolEntity,
			List<DailyQuoteEntity> dailyQuoteEntities) {
		final Map<LocalDate, DailyQuoteEntity> indexQuoteMap = dailyQuoteEntities.stream()
				.collect(Collectors.toMap(DailyQuoteEntity::getLocalDay, entity -> entity));
		final List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> sortedPortfolioChanges = this
				.calculateSortedPortfolioChanges(ptsEntities, symbolEntity, dailyQuoteEntities);
		Mono<Map<LocalDate, DailyQuoteEntity>> comparisonIndexQuotes = null;
		if (symbolEntity.getSymbol().contains(RefMarker.EUROPE_REF_MARKER.getMarker())) {
			comparisonIndexQuotes = this.dailyQuoteRepository.findBySymbol(ComparisonIndexes.EUROSTOXX50.getSymbol())
					.collectMap(DailyQuoteEntity::getLocalDay);
		} else if (symbolEntity.getSymbol().contains(RefMarker.CHINA_REF_MARKER.getMarker())) {
			comparisonIndexQuotes = this.dailyQuoteRepository.findBySymbol(ComparisonIndexes.MSCI_CHINA.getSymbol())
					.collectMap(DailyQuoteEntity::getLocalDay);
		} else if (symbolEntity.getSymbol().contains(RefMarker.US_REF_MARKER.getMarker())) {
			comparisonIndexQuotes = this.dailyQuoteRepository.findBySymbol(ComparisonIndexes.SP500.getSymbol())
					.collectMap(DailyQuoteEntity::getLocalDay);
		} else {
			throw new RuntimeException(String.format("Not a comparison index symbol: %s", symbolEntity.getSymbol()));
		}
		return comparisonIndexQuotes
				.flatMap(quoteMap -> this.recalculateComparisonIndex(quoteMap, indexQuoteMap, sortedPortfolioChanges));
	}

	private Mono<Long> recalculateComparisonIndex(Map<LocalDate, DailyQuoteEntity> comparisonIndexQuoteMap,
			Map<LocalDate, DailyQuoteEntity> currentIndexQuoteMap,
			List<Tuple3<PortfolioToSymbolEntity, SymbolEntity, DailyQuoteEntity>> sortedPortfolioChanges) {
		final List<LocalDate> comparisonIndexDates = comparisonIndexQuoteMap.keySet().stream().sorted()
				.collect(Collectors.toList());
		
		return Mono.empty();
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
