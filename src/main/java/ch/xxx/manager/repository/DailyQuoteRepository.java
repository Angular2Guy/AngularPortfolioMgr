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
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.entity.DailyQuote;
import reactor.core.publisher.Flux;

@Repository
public interface DailyQuoteRepository extends JpaRepository<DailyQuote, Long> {
	@Query("select * from daily_quote where symbol.symbol = :symbol order by local_day asc")
	List<DailyQuote> findBySymbol(String symbol);
	
	@Query("select * from daily_quote where symbol.symbol.id = :symbolId order by local_day asc")
	Flux<DailyQuote> findBySymbolId(Long symbolId);
	
	@Query("select * from daily_quote where symbol.symbol = :symbol and local_day between :start and :end order by local_day asc")
	Flux<DailyQuote> findBySymbolAndDayBetween(String symbol, LocalDate start, LocalDate end);
}
