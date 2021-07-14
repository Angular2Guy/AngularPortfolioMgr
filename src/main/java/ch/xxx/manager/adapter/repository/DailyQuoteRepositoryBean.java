package ch.xxx.manager.adapter.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;

@Repository
public class DailyQuoteRepositoryBean implements DailyQuoteRepository{
	private final JpaDailyQuoteRepository jpaDailyQuoteRepository;
	
	public DailyQuoteRepositoryBean(JpaDailyQuoteRepository jpaDailyQuoteRepository) {
		this.jpaDailyQuoteRepository = jpaDailyQuoteRepository;
	}

	@Override
	public List<DailyQuote> findBySymbol(String symbol) {
		return this.jpaDailyQuoteRepository.findBySymbol(symbol);
	}

	@Override
	public List<DailyQuote> findBySymbolId(Long symbolId) {
		return this.jpaDailyQuoteRepository.findBySymbolId(symbolId);
	}

	@Override
	public List<DailyQuote> findBySymbolAndDayBetween(String symbol, LocalDate start, LocalDate end) {
		return this.jpaDailyQuoteRepository.findBySymbolAndDayBetween(symbol, start, end);
	}
}
