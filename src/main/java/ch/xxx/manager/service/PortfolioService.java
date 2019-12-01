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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.dto.PortfolioDto;
import ch.xxx.manager.entity.PortfolioEntity;
import ch.xxx.manager.repository.PortfolioRepository;
import reactor.core.publisher.Flux;


@Service
public class PortfolioService {
	@Autowired
	private PortfolioRepository portfolioRepository;
	
	public Flux<PortfolioDto> getPortfolioByUserId(Long userId) {
		return this.portfolioRepository.findByUserId(userId).flatMapSequential(entity -> convert(entity));
	}
	
	private Flux<PortfolioDto> convert(PortfolioEntity entity) {
		PortfolioDto dto = new PortfolioDto(entity.getId(), entity.getUserId(), entity.getName());
		return Flux.just(dto);
	}
}
