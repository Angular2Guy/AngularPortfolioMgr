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
package ch.xxx.manager.service;

import java.time.LocalDateTime;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.query.Query;

import ch.xxx.manager.entity.IntraDayQuoteEntity;
import reactor.core.publisher.Flux;

public interface IntraDayQuoteRepository extends R2dbcRepository<IntraDayQuoteEntity, Long> {
	@Query("select * from intradayquote where symbol = :symbol order by localdatetime asc")
	Flux<IntraDayQuoteEntity> findBySymbol(String symbol);
	@Query("select * from intradayquote where symbol = :symbol and localdatetime between :start and :end order by localdatetime asc")
	Flux<IntraDayQuoteEntity> findBySymbolAndLocaldatetimeBetween(String symbol, LocalDateTime start, LocalDateTime end);
}
