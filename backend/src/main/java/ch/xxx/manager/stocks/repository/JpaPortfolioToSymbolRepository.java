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
package ch.xxx.manager.stocks.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ch.xxx.manager.stocks.entity.PortfolioToSymbol;

public interface JpaPortfolioToSymbolRepository extends JpaRepository<PortfolioToSymbol, Long> {
	@Query("select pts from PortfolioToSymbol pts where pts.portfolio.id = :portfolioId")
	List<PortfolioToSymbol> findByPortfolioId(@Param(value = "portfolioId") Long portfolioId);

	@Query("select pts from PortfolioToSymbol pts where pts.symbol.id = :symbolId")
	List<PortfolioToSymbol> findBySymbolId(@Param(value = "symbolId") Long symbolId);

	@Query("select pts from PortfolioToSymbol pts where pts.symbol.id = :symbolId and pts.portfolio.id = :portfolioId")
	List<PortfolioToSymbol> findByPortfolioIdAndSymbolId(@Param(value = "portfolioId") Long portfolioId,
			@Param(value = "symbolId") Long symbolId);

	@Query("select pts from PortfolioToSymbol pts inner join pts.symbol s inner join pts.portfolio p where p.id = :portfolioId")
	List<PortfolioToSymbol> findPortfolioCalcEntitiesByPortfolioId(@Param(value = "portfolioId") Long portfolioId);
}
