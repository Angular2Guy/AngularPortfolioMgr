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

import org.springframework.stereotype.Repository;

@Repository
public class PortfolioAndSymbolRepository {
//	@Autowired
//	private DatabaseClient client;
//
//	public Flux<PortfolioAndSymbol> findPortfolioCalcEntitiesByPortfolioId(Long portfolioId) {
//		return client
//				.execute("select s.symbol as symbol, s.name as symbol_name, s.curr as curr, s.id as symbol_id, "
//						+ "pts.changed_at as changed_at, pts.removed_at as removed_at, pts.weight as weight, "
//						+ "p.id as id, p.user_id as user_id, p.created_at as created_at, p.name as portfolio_name "
//						+ "from portfolio p, portfolio_to_symbol pts, symbol s "
//						+ "where p.id = pts.portfolio_id and pts.symbol_id = s.id and p.id = :portfolioId")
//				.bind("portfolioId", portfolioId).as(PortfolioAndSymbol.class).fetch().all();
//	}
}
