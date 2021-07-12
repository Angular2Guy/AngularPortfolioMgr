package ch.xxx.manager.usecase.service;

import java.util.List;

import ch.xxx.manager.domain.model.dto.HkSymbolImportDto;
import reactor.core.publisher.Mono;

public interface HkexClient {
	Mono<List<HkSymbolImportDto>> importSymbols();
}
