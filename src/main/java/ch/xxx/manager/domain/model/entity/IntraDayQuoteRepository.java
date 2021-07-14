package ch.xxx.manager.domain.model.entity;

import java.time.LocalDateTime;
import java.util.List;

public interface IntraDayQuoteRepository {
	List<IntraDayQuote> findBySymbol(String symbol);
	List<IntraDayQuote> findBySymbolId(Long symbolId);
	List<IntraDayQuote> findBySymbolAndLocaldatetimeBetween(String symbol, LocalDateTime start, LocalDateTime end);
}
