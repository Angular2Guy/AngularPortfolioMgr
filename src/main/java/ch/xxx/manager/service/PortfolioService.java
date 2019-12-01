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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.dto.PortfolioDto;
import ch.xxx.manager.entity.PortfolioEntity;
import ch.xxx.manager.entity.PortfolioToSymbolEntity;
import ch.xxx.manager.repository.PortfolioRepository;
import ch.xxx.manager.repository.PortfolioToSymbolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class PortfolioService {
	@Autowired
	private PortfolioRepository portfolioRepository;
	@Autowired
	private PortfolioToSymbolRepository portfolioToSymbolRepository;
	
	public Flux<PortfolioDto> getPortfolioByUserId(Long userId) {
		return this.portfolioRepository.findByUserId(userId).flatMapSequential(entity -> convert(entity));
	}
	
	public Mono<Boolean> addPortfolio(PortfolioDto dto) {
		return this.portfolioRepository.save(this.convert(dto)).flatMap(myEntity -> Mono.just(myEntity.getId() != null));
	}
	
	public Mono<Boolean> addSymbolToPortfolio(PortfolioDto dto, Long symbolId) {
		return this.portfolioToSymbolRepository.save(this.createPtsEntity(dto, symbolId)).flatMap(myEntity -> Mono.just(myEntity.getId() != null));
	}
	
	public Mono<Boolean> updatePortfolioSymbolWeight(PortfolioDto dto, Long symbolId, BigDecimal weight) {
		return this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(dto.getId(), symbolId)
				.flatMap(myEntity -> Flux.just(this.updatePtsEntity(myEntity, weight)))
				.flatMap(newEntity -> Flux.just(this.portfolioToSymbolRepository.save(newEntity)))
						.count()
						.flatMap(num -> Mono.just(num > 0));
	}
	
	public Mono<Boolean> removeSymbolFromPortfolio(Long portfolioId, Long symbolId) {
		return this.portfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId)
				.flatMap(entity -> Flux.just(this.portfolioToSymbolRepository.delete(entity)))
				.count()
				.flatMap(num -> Mono.just(num > 0));				
	}
	
	private PortfolioToSymbolEntity updatePtsEntity(PortfolioToSymbolEntity entity, BigDecimal weight) {
		entity.setWeight(weight);
		return entity;
	}
	
	private PortfolioToSymbolEntity createPtsEntity(PortfolioDto dto, Long symbolId) {
		PortfolioToSymbolEntity entity = new PortfolioToSymbolEntity();
		entity.setPortfolioId(dto.getId());
		entity.setSymbolId(symbolId);
		entity.setWeight(BigDecimal.ZERO);
		return entity;
	}
	
	private PortfolioEntity convert(PortfolioDto dto) {
		PortfolioEntity entity = new PortfolioEntity();
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setUserId(dto.getUserId());
		return entity;
	}
	
	private Flux<PortfolioDto> convert(PortfolioEntity entity) {
		PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName());
		return Flux.just(dto);
	}
}
