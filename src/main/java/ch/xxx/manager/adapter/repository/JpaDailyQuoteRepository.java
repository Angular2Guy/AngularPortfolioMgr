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

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ch.xxx.manager.domain.model.entity.DailyQuote;

public interface JpaDailyQuoteRepository extends JpaRepository<DailyQuote, Long> {
	@Query("select dq from DailyQuote dq where dq.symbol.symbol = :symbol order by dq.localDay asc")
	List<DailyQuote> findBySymbol(String symbol);
	
	@Query("select dq from DailyQuote dq where dq.symbol.symbol.id = :symbolId order by dq.localDay asc")
	List<DailyQuote> findBySymbolId(Long symbolId);
	
	@Query("select dq from DailyQuote dq where dq.symbol.symbol.id in(:symbolIds) order by dq.localDay asc")
	List<DailyQuote> findBySymbolIds(List<Long> symbolIds);
	
	@Query("select dq from DailyQuote dq where dq.symbol.symbol = :symbol and dq.localDay between :start and :end order by dq.localDay asc")
	List<DailyQuote> findBySymbolAndDayBetween(String symbol, LocalDate start, LocalDate end);
}
