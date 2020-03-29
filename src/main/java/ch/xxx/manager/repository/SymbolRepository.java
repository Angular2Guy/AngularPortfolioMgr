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
package ch.xxx.manager.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.entity.SymbolEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SymbolRepository extends R2dbcRepository<SymbolEntity, Long> {
	@Query("select * from symbol s where lower(s.symbol) = :symbol")
	Mono<SymbolEntity> findBySymbol(String symbol);
	@Query("select * from symbol s where lower(s.name) like :name")
	Flux<SymbolEntity> findByName(String name);
	@Query("select * from symbol s, portfoliotosymbol pts where s.id = pts.symbol_id and pts.portfolio_id = :portfolioId")
	Flux<SymbolEntity> findByPortfolioId(Long portfolioId);
}
