package ch.xxx.manager.usecase.service;

import java.util.List;

import ch.xxx.manager.domain.model.dto.HkDailyQuoteImportDto;
import reactor.core.publisher.Mono;

public interface YahooClient {
	Mono<List<HkDailyQuoteImportDto>> getTimeseriesDailyHistory(String symbol);
}
