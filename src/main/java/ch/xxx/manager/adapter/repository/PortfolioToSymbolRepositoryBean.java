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

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbolRepository;

@Repository
public class PortfolioToSymbolRepositoryBean implements PortfolioToSymbolRepository{
	private final JpaPortfolioToSymbolRepository jpaPortfolioToSymbolRepository;
	
	public PortfolioToSymbolRepositoryBean(JpaPortfolioToSymbolRepository jpaPortfolioToSymbolRepository) {
		this.jpaPortfolioToSymbolRepository = jpaPortfolioToSymbolRepository;
	}

	@Override
	public List<PortfolioToSymbol> findByPortfolioId(Long portfolioId) {
		return this.jpaPortfolioToSymbolRepository.findByPortfolioId(portfolioId);
	}

	@Override
	public List<PortfolioToSymbol> findBySymbolId(Long symbolId) {
		return this.jpaPortfolioToSymbolRepository.findBySymbolId(symbolId);
	}

	@Override
	public List<PortfolioToSymbol> findByPortfolioIdAndSymbolId(Long portfolioId, Long symbolId) {
		return this.jpaPortfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId);
	}

	@Override
	public PortfolioToSymbol save(PortfolioToSymbol portfolioToSymbol) {
		return this.jpaPortfolioToSymbolRepository.save(portfolioToSymbol);
	}
	
	
}
