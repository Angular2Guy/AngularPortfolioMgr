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
package ch.xxx.manager.adapter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioRepository;
import ch.xxx.manager.domain.model.entity.dto.PortfolioAndSymbolDto;

@Repository
public class PortfolioRepositoryBean implements PortfolioRepository{
	private final JpaPortfolioRepository jpaPortfolioRepository;
	
	public PortfolioRepositoryBean(JpaPortfolioRepository jpaPortfolioRepository) {
		this.jpaPortfolioRepository = jpaPortfolioRepository;
	}

	@Override
	public List<Portfolio> findByUserId(Long userId) {
		return this.jpaPortfolioRepository.findByUserId(userId);
	}

	@Override
	public List<PortfolioAndSymbolDto> findPortfolioCalcEntitiesByPortfolioId(Long portfolioId) {
		return this.jpaPortfolioRepository.findPortfolioCalcEntitiesByPortfolioId(portfolioId);
	}

	@Override
	public Optional<Portfolio> findById(Long id) {
		return this.jpaPortfolioRepository.findById(id);
	}

	@Override
	public Portfolio save(Portfolio portfolio) {
		return this.jpaPortfolioRepository.save(portfolio);
	}

	@Override
	public Long countPortfolioSymbolsByUserId(Long userId) {
		return this.jpaPortfolioRepository.countPortfolioSymbolsByUserId(userId);
	}
	
	@Override
	public List<Portfolio> findAllWithPts() {
		return this.jpaPortfolioRepository.findAllWithPts();
	}
}
