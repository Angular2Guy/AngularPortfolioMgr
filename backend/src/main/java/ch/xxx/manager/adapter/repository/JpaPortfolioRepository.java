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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.dto.PortfolioAndSymbolDto;

public interface JpaPortfolioRepository extends JpaRepository<Portfolio, Long> {
	@Query("select p from Portfolio p inner join p.portfolioToSymbols where p.appUser.id = :userId")
	List<Portfolio> findByUserId(@Param(value = "userId") Long userId);
	@Query("select new ch.xxx.manager.domain.model.entity.dto.PortfolioAndSymbolDto(p.id, au.id, p.name, p.createdAt, pts.weight, pts.changedAt, "
			+ "pts.removedAt, s.id, s.symbol, s.name, s.currencyKey) "
			+ "from Portfolio p inner join p.appUser au inner join p.portfolioToSymbols pts inner join pts.symbol s "
			+ "where p.id = :portfolioId and (pts.removedAt is null or pts.removedAt > CURRENT_TIMESTAMP)")
	List<PortfolioAndSymbolDto> findPortfolioCalcEntitiesByPortfolioId(@Param(value = "portfolioId") Long portfolioId);
}
