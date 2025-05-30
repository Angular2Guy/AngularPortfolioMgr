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
package ch.xxx.manager.domain.model.entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;

public interface SymbolRepository {
	List<Symbol> findBySymbol(String symbol);
	List<Symbol> findBySymbolSingle(String symbol);
	List<Symbol> findBySymbolSingleWithQuotes(String symbol);
	List<Symbol> findByName(String name);
	List<Symbol> findByPortfolioId(Long portfolioId);
	Collection<Symbol> findByCikIn(Iterable<String> ciks);
	Collection<Symbol> findBySymbolIn(Iterable<String> symbols);
	List<Symbol> findAll();
	Optional<Symbol> findById(Long id);
	Symbol save(Symbol symbol);
	List<Symbol> saveAll(Iterable<Symbol> symbols);
	List<Symbol> findByQuoteSource(QuoteSource quoteSource);
	void deleteAll(Iterable<Symbol> symbols);
	Optional<Symbol> findByIdWithDailyQuotes(Long symbolId);
}
