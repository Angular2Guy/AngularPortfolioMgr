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
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.SymbolRepository;

@Repository
public class SymbolRepositoryBean implements SymbolRepository {
	private final JpaSymbolRepository jpaSymbolRepository;

	public SymbolRepositoryBean(JpaSymbolRepository jpaSymbolRepository) {
		this.jpaSymbolRepository = jpaSymbolRepository;
	}

	@Override
	public List<Symbol> findBySymbol(String symbol) {
		return symbol == null || symbol.isBlank() ? List.of()
				: this.jpaSymbolRepository.findBySymbol(symbol.trim().toLowerCase());
	}

	@Override
	public Optional<Symbol> findBySymbolSingle(String symbol) {
		return Optional.ofNullable(symbol).stream()
				.flatMap(
						mySymbol -> this.jpaSymbolRepository.findBySymbolSingle(mySymbol.trim().toLowerCase()).stream())
				.findFirst();
	}

	@Override
	public List<Symbol> findByName(String name) {
		return name == null || name.isBlank() ? List.of()
				: this.jpaSymbolRepository.findByName(name.trim().toLowerCase());
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
	public List<Symbol> saveAll(Iterable<Symbol> symbols) {
		return this.jpaSymbolRepository.saveAll(symbols);
	}

	@Override
	public Optional<Symbol> findById(Long id) {
		return this.jpaSymbolRepository.findById(id);
	}

	@Override
	public List<Symbol> findByQuoteSource(QuoteSource quoteSource) {
		return this.jpaSymbolRepository.findByQuoteSource(quoteSource);
	}

	@Override
	public void deleteAll(Iterable<Symbol> symbols) {
		this.jpaSymbolRepository.deleteAll(symbols);
	}

	@Override
	public Optional<Symbol> findByIdWithDailyQuotes(Long symbolId) {
		return this.jpaSymbolRepository.findByIdWithDailyQuotes(symbolId);
	}
}
