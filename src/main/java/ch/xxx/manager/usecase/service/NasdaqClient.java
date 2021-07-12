package ch.xxx.manager.usecase.service;

import java.util.List;

import reactor.core.publisher.Mono;

public interface NasdaqClient {
	Mono<List<String>> importSymbols();
}
