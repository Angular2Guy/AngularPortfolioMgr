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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;

public interface JpaSymbolRepository extends JpaRepository<Symbol, Long> {	
	@Query("select s from Symbol s where lower(s.symbol) like %:symbol%")
	List<Symbol> findBySymbol(@Param(value = "symbol") String symbol);
	@Query("select s from Symbol s where lower(s.symbol) = :symbol")
	List<Symbol> findBySymbolSingle(@Param(value = "symbol") String symbol);
	@Query("select s from Symbol s join fetch s.dailyQuotes where lower(s.symbol) = :symbol")
	List<Symbol> findBySymbolSingleWithQuotes(@Param(value = "symbol") String symbol);
	@Query("select s from Symbol s where lower(s.name) like %:name%")
	List<Symbol> findByName(@Param(value = "name") String name);
	@Query("select s from Symbol s, PortfolioToSymbol pts where s.id = pts.symbol.id and pts.portfolio.id = :portfolioId")
	List<Symbol> findByPortfolioId(@Param(value = "portfolioId") Long portfolioId);
	@Query("select s from Symbol s join fetch s.dailyQuotes where s.quoteSource = :quoteSource")
	List<Symbol> findByQuoteSource(@Param(value="quoteSource") QuoteSource quoteSource);
	@Query("select s from Symbol s join fetch s.dailyQuotes where s.id = :symbolId")
	Optional<Symbol> findByIdWithDailyQuotes(@Param(value="symbolId") Long symbolId);
	Collection<Symbol> findByCikIn(Iterable<String> ciks);
	Collection<Symbol> findBySymbolIn(Iterable<String> symbols);
}
