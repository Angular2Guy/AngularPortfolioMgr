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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.entity.DailyQuoteEntity;
import ch.xxx.manager.entity.PortfolioToSymbolEntity;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.repository.DailyQuoteRepository;
import ch.xxx.manager.repository.PortfolioToSymbolRepository;
import ch.xxx.manager.repository.SymbolRepository;
import reactor.core.publisher.Mono;

@Service
public class PortfolioToIndexService {
	@Autowired
	private PortfolioToSymbolRepository portfolioToSymbolRepository;
	@Autowired
	private SymbolRepository symbolRepository;
	@Autowired
	private DailyQuoteRepository dailyQuoteRepository;

	public Mono<List<Long>> calculateIndexComparison(Long portfolioId) {
		final List<String> refMarkerStrs = List.of(ServiceUtils.RefMarker.values()).stream()
				.map(refMarker -> refMarker.getMarker()).collect(Collectors.toList());
		return this.portfolioToSymbolRepository
				.findByPortfolioId(
						portfolioId)
				.collectList().flatMap(
						ptsEntities -> this.symbolRepository
								.findAllById(ptsEntities.stream().map(ptsEntity -> ptsEntity.getSymbolId()).distinct()
										.collect(Collectors.toList()))
								.filter(symbolEntity -> refMarkerStrs.stream().anyMatch(
										refMarkerStr -> symbolEntity.getSymbol().contains(refMarkerStr)))
								.flatMap(symbolEntity -> this.dailyQuoteRepository
										.findBySymbol(symbolEntity.getSymbol()).collectList()
										.flatMap(dailyQuoteEntities -> this.calculateIndexes(ptsEntities, symbolEntity,
												dailyQuoteEntities))
										.flux())
								.collectList());
	}

	private Mono<Long> calculateIndexes(List<PortfolioToSymbolEntity> ptsEntities, SymbolEntity symbolEntity,
			List<DailyQuoteEntity> dailyQuoteEntities) {
		return Mono.empty();
	}
}
