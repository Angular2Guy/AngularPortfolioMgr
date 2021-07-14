package ch.xxx.manager.domain.model.entity;

import java.time.LocalDate;
import java.util.List;

public interface DailyQuoteRepository {
	List<DailyQuote> findBySymbol(String symbol);

	List<DailyQuote> findBySymbolId(Long symbolId);

	List<DailyQuote> findBySymbolAndDayBetween(String symbol, LocalDate start, LocalDate end);
}
