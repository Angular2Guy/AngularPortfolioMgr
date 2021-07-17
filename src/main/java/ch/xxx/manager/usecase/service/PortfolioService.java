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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.exception.ResourceNotFoundException;
import ch.xxx.manager.domain.model.dto.PortfolioDto;
import ch.xxx.manager.domain.model.dto.SymbolDto;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioRepository;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbolRepository;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.SymbolRepository;
import ch.xxx.manager.domain.utils.CurrencyKey;

@Service
@Transactional
public class PortfolioService {
	private final PortfolioRepository portfolioRepository;
	private final PortfolioToSymbolRepository portfolioToSymbolRepository;
	private final SymbolRepository symbolRepository;
	private final AppUserRepository appUserRepository;
	private final PortfolioCalculationService portfolioCalculationService;

	public PortfolioService(PortfolioRepository portfolioRepository, AppUserRepository appUserRepository,
			PortfolioToSymbolRepository portfolioToSymbolRepository, SymbolRepository symbolRepository,
			PortfolioCalculationService portfolioCalculationService) {
		this.portfolioRepository = portfolioRepository;
		this.portfolioToSymbolRepository = portfolioToSymbolRepository;
		this.symbolRepository = symbolRepository;
		this.portfolioCalculationService = portfolioCalculationService;
		this.appUserRepository = appUserRepository;
	}

	public List<PortfolioDto> getPortfoliosByUserId(Long userId) {
		return this.portfolioRepository.findByUserId(userId).stream().flatMap(entity -> Stream.of(this.convert(entity)))
				.collect(Collectors.toList());
	}

	public PortfolioDto getPortfolioById(Long portfolioId) {
		return this.portfolioRepository.findById(portfolioId).map(entity -> this.convert(entity)).orElse(null);
	}

	public PortfolioDto addPortfolio(PortfolioDto dto) {
		return this.convert(this.portfolioRepository.save(this.convert(dto)));
	}

	public PortfolioDto addSymbolToPortfolio(PortfolioDto dto, Long symbolId, Long weight,
			LocalDateTime changedAt) {
		return this.convert(this.portfolioToSymbolRepository
						.save(this.createPtsEntity(dto, symbolId, weight, changedAt.toLocalDate())).getPortfolio());
	}

//	public Mono<PortfolioDto> updatePortfolioSymbolWeight(PortfolioDto dto, Long symbolId, Long weight,
//			LocalDateTime changedAt) {
//		return Mono
//				.just(this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(dto.getId(), symbolId)
//						.flatMap(myEntity -> Flux.just(this.updatePtsEntity(myEntity, Optional.of(weight),
//								changedAt.toLocalDate(), Optional.empty())))
//						.flatMap(newEntity -> Flux.just(this.portfolioToSymbolRepository.save(newEntity))).count()
//						.block())
//				.flatMap(num -> this.portfolioCalculationService.calculatePortfolio(dto.getId()))
//				.flatMap(entity -> this.convert(entity));
//	}
//
//	public Mono<PortfolioDto> removeSymbolFromPortfolio(Long portfolioId, Long symbolId, LocalDateTime removedAt) {
//		return Mono
//				.just(this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)
//						.flatMap(entity -> Flux.just(this.portfolioToSymbolRepository.save(this.updatePtsEntity(entity,
//								Optional.empty(), LocalDate.now(), Optional.of(removedAt.toLocalDate())))))
//						.count().block())
//				.flatMap(num -> this.portfolioCalculationService.calculatePortfolio(portfolioId))
//				.flatMap(entity -> this.convert(entity));
//	}

	private PortfolioToSymbol updatePtsEntity(PortfolioToSymbol entity, Optional<Long> weightOpt, LocalDate changedAt,
			Optional<LocalDate> removedAtOpt) {
		weightOpt.ifPresent(weight -> entity.setWeight(weight));
		removedAtOpt.ifPresentOrElse(date -> entity.setRemovedAt(date), () -> entity.setChangedAt(changedAt));
		return entity;
	}

	private PortfolioToSymbol createPtsEntity(PortfolioDto dto, Long symbolId, Long weight, LocalDate changedAt) {
		PortfolioToSymbol entity = new PortfolioToSymbol();
		entity.setPortfolio(this.portfolioRepository.findById(dto.getId()).orElse(null));
		entity.setSymbol(this.symbolRepository.findById(symbolId).orElse(null));
		entity.setWeight(weight);
		entity.setChangedAt(changedAt);
		return entity;
	}

	private Portfolio convert(PortfolioDto dto) {
		Portfolio entity = new Portfolio();
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setAppUser(this.appUserRepository.findById(dto.getId()).orElse(null));
		return entity;
	}

	private PortfolioDto updatePortfolioDto(PortfolioToSymbol entity, Symbol symbolEntity, PortfolioDto dto) {
		SymbolDto symbolDto = new SymbolDto(symbolEntity.getId(), symbolEntity.getSymbol(), symbolEntity.getName(),
				entity.getChangedAt(), entity.getRemovedAt(), symbolEntity.getQuoteSource().name());
		symbolDto.setWeight(entity.getWeight());
		dto.getSymbols().add(symbolDto);
		return dto;
	}

	private PortfolioDto convert(PortfolioToSymbol entity, PortfolioDto dto) {
		return entity.getId() == null ? dto
				: this.symbolRepository.findById(entity.getSymbol().getId())
						.map(symbolEntity -> updatePortfolioDto(entity, symbolEntity, dto)).orElse(dto);
	}

	private PortfolioDto convert(Portfolio entity) {
		final PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getAppUser().getId(), entity.getName(),
				entity.getCreatedAt().atStartOfDay(), entity.getMonth1(), entity.getMonth6(), entity.getYear1(),
				entity.getYear2(), entity.getYear5(), entity.getYear10());
		List<PortfolioToSymbol> portfolioToSymbols = this.portfolioToSymbolRepository.findByPortfolioId(dto.getId());
		return portfolioToSymbols.isEmpty() ? this.convert(new PortfolioToSymbol(), dto)
				: portfolioToSymbols.stream().map(pts -> this.convert(pts, dto))
						.filter(myDto -> myDto.getId().equals(dto.getId())).findFirst()
						.orElseThrow(() -> new ResourceNotFoundException("Should not happen."));
	}

	private PortfolioToSymbol createPortfolioPtSAndSymbol(Portfolio portfolioEntity) {
		Symbol symbolEntity = new Symbol(null, ServiceUtils.generateRandomPortfolioSymbol(), portfolioEntity.getName(),
				CurrencyKey.EUR, QuoteSource.PORTFOLIO, Set.of(), Set.of(), Set.of());
		return this.createPtSEntity(portfolioEntity, this.symbolRepository.save(symbolEntity));
	}

	private PortfolioToSymbol createPtSEntity(Portfolio portfolioEntity, Symbol symbol) {
		PortfolioToSymbol ptSEntity = new PortfolioToSymbol();
		ptSEntity.setChangedAt(portfolioEntity.getCreatedAt());
		ptSEntity.setPortfolio(portfolioEntity);
		ptSEntity.setWeight(1L);
		ptSEntity.setSymbol(symbol);
		return ptSEntity;
	}
}
