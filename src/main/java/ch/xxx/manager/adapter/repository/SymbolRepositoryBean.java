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

import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.SymbolRepository;

@Repository
public class SymbolRepositoryBean implements SymbolRepository {
	private final JpaSymbolRepository jpaSymbolRepository;
	
	public SymbolRepositoryBean(JpaSymbolRepository jpaSymbolRepository) {
		this.jpaSymbolRepository = jpaSymbolRepository;
	}

	@Override
	public List<Symbol> findBySymbol(String symbol) {
		return this.jpaSymbolRepository.findBySymbol(symbol);
	}

	@Override
	public List<Symbol> findBySymbolSingle(String symbol) {
		return this.jpaSymbolRepository.findBySymbolSingle(symbol);
	}

	@Override
	public List<Symbol> findByName(String name) {
		return this.jpaSymbolRepository.findByName(name);
	}

	@Override
	public List<Symbol> findByPortfolioId(Long portfolioId) {
		return this.jpaSymbolRepository.findByPortfolioId(portfolioId);
	}
	
	@Override
	public List<Symbol> findAll() {
		return this.jpaSymbolRepository.findAll();
	}

	@Override
	public Symbol save(Symbol symbol) {
		return this.jpaSymbolRepository.save(symbol);
	}

	@Override
	public Optional<Symbol> findById(Long id) {
		return this.jpaSymbolRepository.findById(id);
	}
	
}
