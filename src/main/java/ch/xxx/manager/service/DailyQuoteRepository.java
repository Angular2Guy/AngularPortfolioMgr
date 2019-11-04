package ch.xxx.manager.service;

import org.springframework.data.r2dbc.repository.query.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ch.xxx.manager.entity.DailyQuoteEntity;
import reactor.core.publisher.Flux;

public interface DailyQuoteRepository extends ReactiveCrudRepository<DailyQuoteEntity, Long> {
	@Query("select * from dailyquote where symbol = :symbol")
	Flux<DailyQuoteEntity> findBySymbol(String symbol);
}
