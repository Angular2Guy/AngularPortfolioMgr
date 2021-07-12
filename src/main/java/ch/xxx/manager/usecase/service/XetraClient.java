package ch.xxx.manager.usecase.service;

import java.util.List;

import reactor.core.publisher.Mono;

public interface XetraClient {
	Mono<List<String>> importXetraSymbols();
}
