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
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.dto.PortfolioDto;
import ch.xxx.manager.dto.SymbolDto;
import ch.xxx.manager.entity.PortfolioEntity;
import ch.xxx.manager.entity.PortfolioToSymbolEntity;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.entity.SymbolEntity.QuoteSource;
import ch.xxx.manager.entity.SymbolEntity.SymbolCurrency;

import ch.xxx.manager.repository.PortfolioRepository;
import ch.xxx.manager.repository.PortfolioToSymbolRepository;
import ch.xxx.manager.repository.SymbolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class PortfolioService {
	@Autowired
	private PortfolioRepository portfolioRepository;
	@Autowired
	private PortfolioToSymbolRepository portfolioToSymbolRepository;
	@Autowired
	private SymbolRepository symbolRepository;
	@Autowired
//	private PortfolioCalculationParallelService portfolioCalculationService;
	private PortfolioCalculationService portfolioCalculationService;

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

	public Mono<PortfolioDto> addSymbolToPortfolio(PortfolioDto dto, Long symbolId, Long weight, LocalDateTime changedAt) {
		return Mono.just(this.portfolioToSymbolRepository
				.save(this.createPtsEntity(dto, symbolId, weight, changedAt.toLocalDate())).block())
				.flatMap(myEntity -> this.portfolioCalculationService.calculatePortfolio(dto.getId()))
				.flatMap(entity -> this.convert(entity));
	}

	public Mono<PortfolioDto> updatePortfolioSymbolWeight(PortfolioDto dto, Long symbolId, Long weight,
			LocalDateTime changedAt) {
		return Mono.just(this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(dto.getId(), symbolId)
				.flatMap(myEntity -> Flux.just(
						this.updatePtsEntity(myEntity, Optional.of(weight), changedAt.toLocalDate(), Optional.empty())))
				.flatMap(newEntity -> Flux.just(this.portfolioToSymbolRepository.save(newEntity))).count().block())
				.flatMap(num -> this.portfolioCalculationService.calculatePortfolio(dto.getId()))
				.flatMap(entity -> this.convert(entity));
	}

	public Mono<PortfolioDto> removeSymbolFromPortfolio(Long portfolioId, Long symbolId, LocalDateTime removedAt) {
		return Mono.just(this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)
				.flatMap(entity -> Flux.just(this.portfolioToSymbolRepository.save(this.updatePtsEntity(entity,
						Optional.empty(), LocalDate.now(), Optional.of(removedAt.toLocalDate()))))).count().block())
				.flatMap(num -> this.portfolioCalculationService.calculatePortfolio(portfolioId))
				.flatMap(entity -> this.convert(entity));
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
				entity.getCreatedAt().atStartOfDay(), entity.getMonth1(), entity.getMonth6(), entity.getYear1(),
				entity.getYear2(), entity.getYear5(), entity.getYear10());
		return this.portfolioToSymbolRepository.findByPortfolioId(dto.getId())
				.switchIfEmpty(Mono.just(new PortfolioToSymbolEntity()))
				.flatMapSequential(p2SymbolEntity -> this.convert(p2SymbolEntity, dto)).distinct().single();
	}

	private Flux<PortfolioDto> convertFlux(PortfolioEntity entity) {
		final PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName(),
				entity.getCreatedAt().atStartOfDay(), entity.getMonth1(), entity.getMonth6(), entity.getYear1(),
				entity.getYear2(), entity.getYear5(), entity.getYear10());
		return this.portfolioToSymbolRepository.findByPortfolioId(dto.getId())
				.switchIfEmpty(this.createPortfolioPtSAndSymbol(entity))
				.flatMapSequential(p2SymbolEntity -> this.convert(p2SymbolEntity, dto)).distinct();
	}

	private Flux<PortfolioToSymbolEntity> createPortfolioPtSAndSymbol(PortfolioEntity portfolioEntity) {
		SymbolEntity symbolEntity = new SymbolEntity(null, ServiceUtils.generateRandomPortfolioSymbol(), portfolioEntity.getName(), SymbolCurrency.EUR, QuoteSource.PORTFOLIO);
		return this.symbolRepository.save(symbolEntity).flatMap(mySymbolEntity -> 
			this.portfolioToSymbolRepository.save(this.createPtSEntity(portfolioEntity, mySymbolEntity.getId()))).flux();
	}

	private PortfolioToSymbolEntity createPtSEntity(PortfolioEntity portfolioEntity, Long symbolId) {
		PortfolioToSymbolEntity ptSEntity = new PortfolioToSymbolEntity();
		ptSEntity.setChangedAt(portfolioEntity.getCreatedAt());
		ptSEntity.setPortfolioId(portfolioEntity.getId());
		ptSEntity.setWeight(1L);
		ptSEntity.setSymbolId(symbolId);
		return ptSEntity;
	}
}
