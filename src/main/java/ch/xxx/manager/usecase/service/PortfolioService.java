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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.adapter.repository.PortfolioRepository;
import ch.xxx.manager.adapter.repository.PortfolioToSymbolRepository;
import ch.xxx.manager.adapter.repository.SymbolRepository;

@Service
@Transactional
public class PortfolioService {
	private final PortfolioRepository portfolioRepository;
	private final PortfolioToSymbolRepository portfolioToSymbolRepository;
	private final SymbolRepository symbolRepository;
	private final PortfolioCalculationService portfolioCalculationService;

	public PortfolioService(PortfolioRepository portfolioRepository, PortfolioToSymbolRepository portfolioToSymbolRepository, SymbolRepository symbolRepository, PortfolioCalculationService portfolioCalculationService) {
		this.portfolioRepository = portfolioRepository;
		this.portfolioToSymbolRepository = portfolioToSymbolRepository;
		this.symbolRepository = symbolRepository;
		this.portfolioCalculationService = portfolioCalculationService;
	}
	
//	public Flux<PortfolioDto> getPortfoliosByUserId(Long userId) {
//		return this.portfolioRepository.findByUserId(userId).flatMapSequential(entity -> this.convertFlux(entity));
//	}
//
//	public Mono<PortfolioDto> getPortfolioById(Long portfolioId) {
//		return this.portfolioRepository.findById(portfolioId).flatMap(entity -> this.convert(entity));
//	}
//
//	public Mono<PortfolioDto> addPortfolio(PortfolioDto dto) {
//		return this.portfolioRepository.save(this.convert(dto))
//				.flatMap(myEntity -> this.convertFlux(myEntity).singleOrEmpty());
//	}
//
//	public Mono<PortfolioDto> addSymbolToPortfolio(PortfolioDto dto, Long symbolId, Long weight, LocalDateTime changedAt) {
//		return Mono.just(this.portfolioToSymbolRepository
//				.save(this.createPtsEntity(dto, symbolId, weight, changedAt.toLocalDate())).block())
//				.flatMap(myEntity -> this.portfolioCalculationService.calculatePortfolio(dto.getId()))
//				.flatMap(entity -> this.convert(entity));
//	}
//
//	public Mono<PortfolioDto> updatePortfolioSymbolWeight(PortfolioDto dto, Long symbolId, Long weight,
//			LocalDateTime changedAt) {
//		return Mono.just(this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(dto.getId(), symbolId)
//				.flatMap(myEntity -> Flux.just(
//						this.updatePtsEntity(myEntity, Optional.of(weight), changedAt.toLocalDate(), Optional.empty())))
//				.flatMap(newEntity -> Flux.just(this.portfolioToSymbolRepository.save(newEntity))).count().block())
//				.flatMap(num -> this.portfolioCalculationService.calculatePortfolio(dto.getId()))
//				.flatMap(entity -> this.convert(entity));
//	}
//
//	public Mono<PortfolioDto> removeSymbolFromPortfolio(Long portfolioId, Long symbolId, LocalDateTime removedAt) {
//		return Mono.just(this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)
//				.flatMap(entity -> Flux.just(this.portfolioToSymbolRepository.save(this.updatePtsEntity(entity,
//						Optional.empty(), LocalDate.now(), Optional.of(removedAt.toLocalDate()))))).count().block())
//				.flatMap(num -> this.portfolioCalculationService.calculatePortfolio(portfolioId))
//				.flatMap(entity -> this.convert(entity));
//	}
//
//	private PortfolioToSymbol updatePtsEntity(PortfolioToSymbol entity, Optional<Long> weightOpt,
//			LocalDate changedAt, Optional<LocalDate> removedAtOpt) {
//		weightOpt.ifPresent(weight -> entity.setWeight(weight));
//		removedAtOpt.ifPresentOrElse(date -> entity.setRemovedAt(date), () -> entity.setChangedAt(changedAt));
//		return entity;
//	}
//
//	private PortfolioToSymbol createPtsEntity(PortfolioDto dto, Long symbolId, Long weight, LocalDate changedAt) {
//		PortfolioToSymbol entity = new PortfolioToSymbol();
//		entity.setPortfolioId(dto.getId());
//		entity.setSymbolId(symbolId);
//		entity.setWeight(weight);
//		entity.setChangedAt(changedAt);
//		return entity;
//	}
//
//	private Portfolio convert(PortfolioDto dto) {
//		Portfolio entity = new Portfolio();
//		entity.setId(dto.getId());
//		entity.setName(dto.getName());
//		entity.setUserId(dto.getUserId());
//		entity.setCreatedAt(dto.getCreatedAt() == null ? LocalDate.now() : dto.getCreatedAt().toLocalDate());
//		return entity;
//	}
//
//	private Mono<PortfolioDto> updatePortfolioDto(PortfolioToSymbol entity, Symbol symbolEntity,
//			PortfolioDto dto) {
//		SymbolDto symbolDto = new SymbolDto(symbolEntity.getId(), symbolEntity.getSymbol(), symbolEntity.getName(),
//				entity.getChangedAt(), entity.getRemovedAt(), symbolEntity.getSource());
//		symbolDto.setWeight(entity.getWeight());
//		dto.getSymbols().add(symbolDto);
//		return Mono.just(dto);
//	}
//
//	private Mono<PortfolioDto> convert(PortfolioToSymbol entity, PortfolioDto dto) {
//		if (entity.getId() == null) {
//			return Mono.just(dto);
//		}
//		return this.symbolRepository.findById(entity.getSymbolId())
//				.flatMap(symbolEntity -> updatePortfolioDto(entity, symbolEntity, dto));
//	}
//
//	private Mono<PortfolioDto> convert(Portfolio entity) {
//		final PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName(),
//				entity.getCreatedAt().atStartOfDay(), entity.getMonth1(), entity.getMonth6(), entity.getYear1(),
//				entity.getYear2(), entity.getYear5(), entity.getYear10());
//		return this.portfolioToSymbolRepository.findByPortfolioId(dto.getId())
//				.switchIfEmpty(Mono.just(new PortfolioToSymbol()))
//				.flatMapSequential(p2SymbolEntity -> this.convert(p2SymbolEntity, dto)).distinct().single();
//	}
//
//	private Flux<PortfolioDto> convertFlux(Portfolio entity) {
//		final PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName(),
//				entity.getCreatedAt().atStartOfDay(), entity.getMonth1(), entity.getMonth6(), entity.getYear1(),
//				entity.getYear2(), entity.getYear5(), entity.getYear10());
//		return this.portfolioToSymbolRepository.findByPortfolioId(dto.getId())
//				.switchIfEmpty(this.createPortfolioPtSAndSymbol(entity))
//				.flatMapSequential(p2SymbolEntity -> this.convert(p2SymbolEntity, dto)).distinct();
//	}
//
//	private Flux<PortfolioToSymbol> createPortfolioPtSAndSymbol(Portfolio portfolioEntity) {
//		Symbol symbolEntity = new Symbol(null, ServiceUtils.generateRandomPortfolioSymbol(), portfolioEntity.getName(), SymbolCurrency.EUR, QuoteSource.PORTFOLIO);
//		return this.symbolRepository.save(symbolEntity).flatMap(mySymbolEntity -> 
//			this.portfolioToSymbolRepository.save(this.createPtSEntity(portfolioEntity, mySymbolEntity.getId()))).flux();
//	}
//
//	private PortfolioToSymbol createPtSEntity(Portfolio portfolioEntity, Long symbolId) {
//		PortfolioToSymbol ptSEntity = new PortfolioToSymbol();
//		ptSEntity.setChangedAt(portfolioEntity.getCreatedAt());
//		ptSEntity.setPortfolioId(portfolioEntity.getId());
//		ptSEntity.setWeight(1L);
//		ptSEntity.setSymbolId(symbolId);
//		return ptSEntity;
//	}
}
