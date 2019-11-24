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

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.query.Query;

import ch.xxx.manager.entity.DailyQuoteEntity;
import reactor.core.publisher.Flux;

public interface DailyQuoteRepository extends R2dbcRepository<DailyQuoteEntity, Long> {
	@Query("select * from dailyquote where symbol = :symbol order by day desc")
	Flux<DailyQuoteEntity> findBySymbol(String symbol);
}
