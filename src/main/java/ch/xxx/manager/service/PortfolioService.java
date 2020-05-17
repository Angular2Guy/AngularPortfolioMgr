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
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.dto.PortfolioDto;
import ch.xxx.manager.dto.SymbolDto;
import ch.xxx.manager.entity.PortfolioEntity;
import ch.xxx.manager.entity.PortfolioToSymbolEntity;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.repository.PortfolioRepository;
import ch.xxx.manager.repository.PortfolioToSymbolRepository;
import ch.xxx.manager.repository.SymbolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PortfolioService {
	@Autowired
	private PortfolioRepository portfolioRepository;
	@Autowired
	private PortfolioToSymbolRepository portfolioToSymbolRepository;
	@Autowired
	private SymbolRepository symbolRepository;

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
		return this.portfolioToSymbolRepository.save(this.createPtsEntity(dto, symbolId, weight, changedAt.toLocalDate()))
				.flatMap(myEntity -> Mono.just(myEntity.getId() != null));
	}

	public Mono<Boolean> updatePortfolioSymbolWeight(PortfolioDto dto, Long symbolId, Long weight, LocalDateTime changedAt) {
		return this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(dto.getId(), symbolId)
				.flatMap(myEntity -> Flux.just(this.updatePtsEntity(myEntity, Optional.of(weight), changedAt.toLocalDate(), Optional.empty())))
				.flatMap(newEntity -> Flux.just(this.portfolioToSymbolRepository.save(newEntity))).count()
				.flatMap(num -> Mono.just(num > 0));
	}

	public Mono<Boolean> removeSymbolFromPortfolio(Long portfolioId, Long symbolId, LocalDateTime removedAt) {
		return this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)
				.flatMap(entity -> Flux.just(this.portfolioToSymbolRepository
						.save(this.updatePtsEntity(entity, Optional.empty(), LocalDate.now(), Optional.of(removedAt.toLocalDate())))))
				.count().flatMap(num -> Mono.just(num > 0));
	}

	private PortfolioToSymbolEntity updatePtsEntity(PortfolioToSymbolEntity entity, Optional<Long> weightOpt, LocalDate changedAt,
			Optional<LocalDate> removedAtOpt) {
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
		final PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName(), entity.getCreatedAt());
		return this.portfolioToSymbolRepository.findByPortfolioId(dto.getId())
				.switchIfEmpty(Mono.just(new PortfolioToSymbolEntity()))
				.flatMapSequential(p2SymbolEntity -> this.convert(p2SymbolEntity, dto)).distinct().single();
	}

	private Flux<PortfolioDto> convertFlux(PortfolioEntity entity) {
		final PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName(), entity.getCreatedAt());
		return this.portfolioToSymbolRepository.findByPortfolioId(dto.getId())
				.switchIfEmpty(Flux.just(new PortfolioToSymbolEntity()))
				.flatMapSequential(p2SymbolEntity -> this.convert(p2SymbolEntity, dto)).distinct();
	}
}
