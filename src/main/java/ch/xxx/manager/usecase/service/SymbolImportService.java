/**
 *    Copyright 2019 Sven Loesekann
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ch.xxx.manager.usecase.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.model.dto.HkSymbolImportDto;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.SymbolRepository;
import ch.xxx.manager.domain.utils.CurrencyKey;

@Service
@Transactional
public class SymbolImportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolImportService.class);
	private final NasdaqClient nasdaqClient;
	private final HkexClient hkexClient;
	private final SymbolRepository repository;
	private final XetraClient xetraClient;
	private final QuoteImportService quoteImportService;
	private final AtomicReference<List<Symbol>> allSymbolEntities = new AtomicReference<>(
			new ArrayList<>());

	public SymbolImportService(NasdaqClient nasdaqClient, HkexClient hkexClient, SymbolRepository repository,
			XetraClient xetraClient, QuoteImportService quoteImportService) {
		this.nasdaqClient = nasdaqClient;
		this.hkexClient = hkexClient;
		this.quoteImportService = quoteImportService;
		this.repository = repository;
		this.xetraClient = xetraClient;
	}

	@EventListener
	public Integer onApplicationEvent(ApplicationReadyEvent event) {
		LOGGER.info("Refresh Symbols");
		return this.refreshSymbolEntities();
	}

	public Integer refreshSymbolEntities() {
		List<Symbol> symbolEnities = this.repository.findAll();
		this.allSymbolEntities.set(symbolEnities);
		LOGGER.info("{} symbols updated.", symbolEnities.size());
		return symbolEnities.size();
	}

	public String importUsSymbols() {
		this.nasdaqClient.importSymbols().subscribe(nasdaq -> this.importUsSymbols(nasdaq));
		return "Nasdaq import";
	}

	public Long importUsSymbols(List<String> nasdaq) {
		LOGGER.info("importUsSymbols() called.");
		return nasdaq.stream().filter(this::filter).flatMap(symbolStr -> this.convert(symbolStr))
				.flatMap(entity -> this.replaceEntity(entity, Optional.empty())).count();
	}

	public String importHkSymbols() {
		this.hkexClient.importSymbols().subscribe(hkex -> this.importHkSymbols(hkex));
		return "Hkex import";
	}

	public Long importHkSymbols(List<HkSymbolImportDto> hkex) {
		LOGGER.info("importHkSymbols() called.");
		return hkex.stream().filter(this::filter).flatMap(myDto -> this.convert(myDto))
				.flatMap(entity -> this.replaceEntity(entity, Optional.empty())).count();
	}

	public String importDeSymbols() {
		this.xetraClient.importXetraSymbols().subscribe(xetra -> this.importDeSymbols(xetra));
		return "Xetra import";
	}

	public Long importDeSymbols(List<String> xetra) {
		LOGGER.info("importDeSymbols() called.");
		return xetra.stream().filter(this::filter).filter(this::filterXetra).flatMap(line -> this.convertXetra(line))
				.collect(Collectors.groupingBy(Symbol::getSymbol)).entrySet().stream()
				.flatMap(group -> group.getValue().isEmpty() ? Stream.empty() : Stream.of(group.getValue().get(0)))
				.flatMap(entity -> this.replaceEntity(entity, Optional.empty())).count();
	}

	public Long importReferenceIndexes(List<String> symbolStrs) {
		LOGGER.info("importReferenceIndexes() called.");
		final List<String> localSymbolStrs = symbolStrs.isEmpty() ? List.of(ComparisonIndex.SP500.getSymbol(),
				ComparisonIndex.EUROSTOXX50.getSymbol(), ComparisonIndex.MSCI_CHINA.getSymbol()) : symbolStrs;
		final Set<String> symbolStrsToImport = new HashSet<>();
		final List<Symbol> availiableSymbolEntities = new ArrayList<>();
		return this.repository.findAll().stream().flatMap(symbolEntities -> {
			availiableSymbolEntities.add(symbolEntities);
			return Stream.of(localSymbolStrs);
		}).flatMap(Collection::stream)
				.filter(symbolStr -> Stream.of(ComparisonIndex.values()).map(ComparisonIndex::getSymbol)
						.anyMatch(indexSymbol -> indexSymbol.equalsIgnoreCase(symbolStr)))
				.flatMap(indexSymbol -> upsertSymbolEntity(indexSymbol, availiableSymbolEntities)).map(entity -> {
					symbolStrsToImport.add(entity.getSymbol());
					return entity;
				}).onClose(() -> {
					this.quoteImportService.importUpdateDailyQuotes(symbolStrsToImport);
					LOGGER.info("Indexquotes import done for: {}", symbolStrsToImport);
				}).count();
	}

	private Stream<Symbol> upsertSymbolEntity(String indexSymbol, List<Symbol> availiableSymbolEntities) {
		ComparisonIndex compIndex = Stream.of(ComparisonIndex.values())
				.filter(index -> index.getSymbol().equalsIgnoreCase(indexSymbol)).findFirst()
				.orElseThrow(() -> new RuntimeException("Unknown indexSymbol: " + indexSymbol));
		Symbol symbolEntity = new Symbol(null, compIndex.getSymbol(), compIndex.getName(), compIndex.getCurrencyKey(),
				compIndex.getSource(), null, null, null);
		return this.replaceEntity(symbolEntity, Optional.of(availiableSymbolEntities));
	}

	private Stream<Symbol> replaceEntity(Symbol entity, Optional<List<Symbol>> availiableSymbolEntitiesOpt) {
		return this.updateEntity(availiableSymbolEntitiesOpt.orElse(this.allSymbolEntities.get()).stream()
				.filter(mySymbol -> mySymbol.getSymbol().toLowerCase().equals(entity.getSymbol().toLowerCase()))
				.findFirst().orElse(this.repository.save(entity)), entity);
	}

	private Stream<Symbol> updateEntity(Symbol dbEntity, Symbol importEntity) {
		if (!dbEntity.equals(importEntity)) {
			dbEntity.setName(importEntity.getName());
			dbEntity.setSymbol(importEntity.getSymbol());
		}
		return Stream.of(dbEntity);
	}

	private Stream<Symbol> convert(HkSymbolImportDto dto) {
		String cutSymbol = dto.getSymbol().trim().length() > 4
				? dto.getSymbol().trim().substring(dto.getSymbol().length() - 4)
				: dto.getSymbol().trim();
		return Stream.of(new Symbol(null, String.format("%s.HK", cutSymbol), dto.getName(), CurrencyKey.HKD,
				QuoteSource.YAHOO, Set.of(), Set.of(), Set.of()));
	}

	private boolean filter(HkSymbolImportDto dto) {
		long symbol = Long.parseLong(dto.getSymbol());
		return symbol < 10000;
	}

	private boolean filter(String line) {
		if (line.isBlank() || line.contains("ACT Symbol|Security Name|Exchange|")
				|| line.contains("File Creation Time:") || line.contains("Symbol|Security Name|Market Category|")
				|| line.contains("Market:") || line.contains("Date Last Update:")
				|| line.contains("Product Status;Instrument Status;")) {
			return false;
		}
		return true;
	}

	private boolean filterXetra(String line) {
		return line.contains("DEUTSCHLAND") || line.contains("DAX");
	}

	private Stream<Symbol> convertXetra(String symbolLine) {
		String[] strParts = symbolLine.split(";");
		String symbol = String.format("%s.DEX",
				strParts[7].substring(0, strParts[7].length() < 15 ? strParts[7].length() : 15));
		Symbol entity = new Symbol(null, symbol,
				strParts[2].substring(0, strParts[2].length() < 100 ? strParts[2].length() : 100), CurrencyKey.EUR,
				QuoteSource.ALPHAVANTAGE, Set.of(), Set.of(), Set.of());
		return Stream.of(entity);
	}

	private Stream<Symbol> convert(String symbolLine) {
		String[] strParts = symbolLine.split("\\|");
		Symbol entity = new Symbol(null,
				strParts[0].substring(0, strParts[0].length() < 15 ? strParts[0].length() : 15),
				strParts[1].substring(0, strParts[1].length() < 100 ? strParts[1].length() : 100), CurrencyKey.USD,
				QuoteSource.ALPHAVANTAGE, Set.of(), Set.of(), Set.of());
		return Stream.of(entity);
	}
}
