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

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ch.xxx.manager.domain.model.entity.IntraDayQuote;

public interface JpaIntraDayQuoteRepository extends JpaRepository<IntraDayQuote, Long> {
	@Query("select idq from IntraDayQuote idq where idq.symbol.symbol = :symbol order by idq.localDateTime asc")
	List<IntraDayQuote> findBySymbol(String symbol);
	@Query("select idq from IntraDayQuote idq where idq.symbol.id = :symbolId order by idq.localDateTime asc")
	List<IntraDayQuote> findBySymbolId(Long symbolId);
	@Query("select idq from IntraDayQuote idq where idq.symbol.symbol = :symbol and idq.localDateTime between :start and :end order by idq.localDateTime asc")
	List<IntraDayQuote> findBySymbolAndLocaldatetimeBetween(String symbol, LocalDateTime start, LocalDateTime end);
}
