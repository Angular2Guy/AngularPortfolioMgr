package ch.xxx.manager.adapter.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.IntraDayQuote;
import ch.xxx.manager.domain.model.entity.IntraDayQuoteRepository;

@Repository
public class IntraDayQuoteRepositoryBean implements IntraDayQuoteRepository{
	private final JpaIntraDayQuoteRepository jpaIntraDayQuoteRepository;
	
	public IntraDayQuoteRepositoryBean(JpaIntraDayQuoteRepository jpaIntraDayQuoteRepository) {
		this.jpaIntraDayQuoteRepository = jpaIntraDayQuoteRepository;
	}

	@Override
	public List<IntraDayQuote> findBySymbol(String symbol) {
		return this.jpaIntraDayQuoteRepository.findBySymbol(symbol);
	}

	@Override
	public List<IntraDayQuote> findBySymbolId(Long symbolId) {
		return this.jpaIntraDayQuoteRepository.findBySymbolId(symbolId);
	}

	@Override
	public List<IntraDayQuote> findBySymbolAndLocaldatetimeBetween(String symbol, LocalDateTime start,
			LocalDateTime end) {
		return this.jpaIntraDayQuoteRepository.findBySymbolAndLocaldatetimeBetween(symbol, start, end);
	}
}
