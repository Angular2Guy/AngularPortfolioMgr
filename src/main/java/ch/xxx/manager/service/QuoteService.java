package ch.xxx.manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.dto.DailyQuoteExportDto;
import ch.xxx.manager.entity.DailyQuoteEntity;
import reactor.core.publisher.Flux;

@Service
public class QuoteService {
	@Autowired
	private DailyQuoteRepository dailyQuoteRepository;
	
	public Flux<DailyQuoteExportDto> getDailyQuotes(String symbol) {
		return this.dailyQuoteRepository.findBySymbol(symbol).flatMapSequential(quote -> convert(quote));
	}
	
	private Flux<DailyQuoteExportDto> convert(DailyQuoteEntity entity) {
		return Flux.just(new DailyQuoteExportDto(entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose(), entity.getVolume(), entity.getDay(), entity.getSymbol()));
	}
}
