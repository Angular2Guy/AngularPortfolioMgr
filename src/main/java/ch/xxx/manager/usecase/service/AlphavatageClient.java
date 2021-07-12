package ch.xxx.manager.usecase.service;

import ch.xxx.manager.domain.model.dto.DailyFxWrapperImportDto;
import ch.xxx.manager.domain.model.dto.DailyWrapperImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayWrapperImportDto;
import reactor.core.publisher.Mono;

public interface AlphavatageClient {
	Mono<IntraDayWrapperImportDto> getTimeseriesIntraDay(String symbol);
	Mono<DailyWrapperImportDto> getTimeseriesDailyHistory(String symbol, boolean fullSeries);
	Mono<DailyFxWrapperImportDto> getFxTimeseriesDailyHistory(String to_currency, boolean fullSeries);
}
