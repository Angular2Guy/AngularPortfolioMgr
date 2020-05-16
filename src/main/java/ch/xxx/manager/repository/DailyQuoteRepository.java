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

import java.time.LocalDate;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.entity.DailyQuoteEntity;
import reactor.core.publisher.Flux;

@Repository
public interface DailyQuoteRepository extends R2dbcRepository<DailyQuoteEntity, Long> {
	@Query("select * from daily_quote where symbol = :symbol order by local_day asc")
	Flux<DailyQuoteEntity> findBySymbol(String symbol);
	
	@Query("select * from daily_quote where symbol_id = :symbolId order by local_day asc")
	Flux<DailyQuoteEntity> findBySymbolId(Long symbolId);
	
	@Query("select * from daily_quote where symbol = :symbol and local_day between :start and :end order by local_day asc")
	Flux<DailyQuoteEntity> findBySymbolAndDayBetween(String symbol, LocalDate start, LocalDate end);
}
