package ch.xxx.manager.service;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.query.Query;

import ch.xxx.manager.entity.DailyQuoteEntity;
import reactor.core.publisher.Flux;

public interface DailyQuoteRepository extends R2dbcRepository<DailyQuoteEntity, Long> {
	@Query("select * from dailyquote where symbol = :symbol")
	Flux<DailyQuoteEntity> findBySymbol(String symbol);
}
