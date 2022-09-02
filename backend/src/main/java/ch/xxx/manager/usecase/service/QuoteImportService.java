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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.model.dto.AlphaOverviewImportDto;
import ch.xxx.manager.domain.model.dto.DailyQuoteImportDto;
import ch.xxx.manager.domain.model.dto.DailyWrapperImportDto;
import ch.xxx.manager.domain.model.dto.HkDailyQuoteImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayMetaDataImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayQuoteImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayWrapperImportDto;
import ch.xxx.manager.domain.model.dto.RapidOverviewImportDto;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.IntraDayQuote;
import ch.xxx.manager.domain.model.entity.IntraDayQuoteRepository;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.SymbolRepository;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class QuoteImportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteImportService.class);
	private final AlphavatageClient alphavatageClient;
	private final YahooClient yahooClient;
	private final RapidApiClient rapidApiClient;
	private final DailyQuoteRepository dailyQuoteRepository;
	private final IntraDayQuoteRepository intraDayQuoteRepository;
	private final SymbolRepository symbolRepository;
	private final CurrencyService currencyService;

	public QuoteImportService(AlphavatageClient alphavatageConnector, YahooClient yahooConnector,
			DailyQuoteRepository dailyQuoteRepository, IntraDayQuoteRepository intraDayQuoteRepository,
			SymbolRepository symbolRepository, CurrencyService currencyService, RapidApiClient rapidApiClient) {
		this.alphavatageClient = alphavatageConnector;
		this.yahooClient = yahooConnector;
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.intraDayQuoteRepository = intraDayQuoteRepository;
		this.symbolRepository = symbolRepository;
		this.currencyService = currencyService;
		this.rapidApiClient = rapidApiClient;
	}

	public Long importIntraDayQuotes(String symbol) {
		return this.importIntraDayQuotes(symbol, null);
	}

	public Long importIntraDayQuotes(String symbol, Duration delay) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		final Duration myTimeout = Duration.ofSeconds(myDelay.get(ChronoUnit.SECONDS) + 10);
		IntraDayWrapperImportDto intraDayWrapperImportDto = new IntraDayWrapperImportDto();
		intraDayWrapperImportDto.setDailyQuotes(new HashMap<String, IntraDayQuoteImportDto>());
		intraDayWrapperImportDto.setMetaData(new IntraDayMetaDataImportDto());

		record SymbolAndWrapper(Symbol symbol, IntraDayWrapperImportDto intraDayWrapperImportDto) {
		}

		LOGGER.info("importIntraDayQuotes() called for symbol: {}", symbol);
		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase()).stream()
				.filter(mySymbol -> QuoteSource.ALPHAVANTAGE.equals(mySymbol.getQuoteSource()))
				.map(mySymbol -> new SymbolAndWrapper(mySymbol,
						this.alphavatageClient.getTimeseriesIntraDay(mySymbol.getSymbol()).delayElement(myDelay)
								.blockOptional(myTimeout).orElse(intraDayWrapperImportDto)))
				.peek(myRecord -> this
						.deleteIntraDayQuotes(this.intraDayQuoteRepository.findBySymbol(myRecord.symbol.getSymbol())))
				.map(myRecord -> this.convert(myRecord.symbol, myRecord.intraDayWrapperImportDto))
				.map(myQuotes -> this.saveAllIntraDayQuotes(myQuotes)).count();
	}

	public Long importDailyQuoteHistory(String symbol) {
		LOGGER.info("importQuoteHistory() called for symbol: {}", symbol);
		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase()).stream()
				.flatMap(symbolEntity -> Stream.of(
						this.customImport(symbol, this.currencyService.getCurrencyMap(), symbolEntity, List.of(), null))
						.flatMap(value -> Stream.of(this.saveAllDailyQuotes(value))))
				.count();
	}

	private List<DailyQuote> customImport(String symbol, Map<LocalDate, Collection<Currency>> currencyMap,
			Symbol symbolEntity, List<DailyQuote> entities, Duration delay) {
		return switch (symbolEntity.getQuoteSource()) {
		case ALPHAVANTAGE -> this.alphavantageImport(symbol, currencyMap, symbolEntity, entities, delay);
		case YAHOO -> this.yahooImport(symbol, currencyMap, symbolEntity, entities, delay);
		default -> List.of();
		};
	}

	public Long importUpdateDailyQuotes(String symbol) {
		LOGGER.info("importNewDailyQuotes() called for symbol: {}", symbol);
		return this.importUpdateDailyQuotes(Set.of(symbol), null);
	}

	public Long importUpdateDailyQuotes(String symbol, Duration delay) {
		LOGGER.info("importNewDailyQuotes() called for symbol: {}", symbol);
		return this.importUpdateDailyQuotes(Set.of(symbol), delay);
	}

	public Long importUpdateDailyQuotes(Set<String> symbols, Duration delay) {
		record SymbolAndQuotes(Symbol symbol, List<DailyQuote> dailyQuotes) {
		}
		return symbols.stream()
				.flatMap(symbol -> Stream.of(this.symbolRepository.findBySymbolSingle(symbol.toLowerCase()).stream()
						.flatMap(symbolEntity -> Stream.of(new SymbolAndQuotes(symbolEntity,
								this.dailyQuoteRepository.findBySymbolId(symbolEntity.getId()))))
						.map(myRecord -> this.customImport(symbol, this.currencyService.getCurrencyMap(),
								myRecord.symbol, myRecord.dailyQuotes, delay))
						.map(values -> this.saveAllDailyQuotes(values)).count()))
				.reduce(0L, (a, b) -> a + b);
	}

	private Symbol overviewImport(String symbol, Symbol symbolEntity, Duration delay) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		final Duration myTimeout = Duration.ofSeconds(myDelay.get(ChronoUnit.SECONDS) + 10);
		final Symbol mySymbolEntity = symbolEntity;
		symbolEntity = switch (mySymbolEntity.getQuoteSource()) {
		case ALPHAVANTAGE ->
			this.alphavatageClient.importCompanyProfile(symbol).delayElement(myDelay).blockOptional(myTimeout).stream()
					.map(myDto -> this.updateSymbol(myDto, mySymbolEntity)).findFirst().orElse(mySymbolEntity);
		case YAHOO -> this.rapidApiClient.importCompanyProfile(symbol).delayElement(myDelay).blockOptional(myTimeout)
				.stream().map(myDto -> this.updateSymbol(myDto, mySymbolEntity)).findFirst().orElse(mySymbolEntity);
		default -> Optional.of(mySymbolEntity).get();
		};
		return symbolEntity;
	}

	private Symbol updateSymbol(RapidOverviewImportDto dto, Symbol symbol) {
		return symbol;
	}

	private Symbol updateSymbol(AlphaOverviewImportDto dto, Symbol symbol) {
		return symbol;
	}

	private List<DailyQuote> yahooImport(String symbol, Map<LocalDate, Collection<Currency>> currencyMap,
			Symbol symbolEntity, List<DailyQuote> entities, Duration delay) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		final Duration myTimeout = Duration.ofSeconds(myDelay.get(ChronoUnit.SECONDS) + 10);
		return entities.isEmpty()
				? this.yahooClient.getTimeseriesDailyHistory(symbol).delayElement(myDelay).blockOptional(myTimeout)
						.map(importDtos -> this.convert(symbolEntity, importDtos, currencyMap)).orElse(List.of())
				: this.yahooClient.getTimeseriesDailyHistory(symbol).delayElement(myDelay).blockOptional(myTimeout)
						.map(importDtos -> this.convert(symbolEntity, importDtos, currencyMap)).orElse(List.of())
						.stream()
						.filter(dto -> entities.stream()
								.noneMatch(myEntity -> myEntity.getLocalDay().isEqual(dto.getLocalDay())))
						.collect(Collectors.toList());
	}

	private List<DailyQuote> convert(Symbol symbolEntity, List<HkDailyQuoteImportDto> importDtos,
			Map<LocalDate, Collection<Currency>> currencyMap) {
		List<DailyQuote> quotes = importDtos.stream()
				.filter(myImportDto -> myImportDto.getAdjClose() != null && myImportDto.getVolume() != null)
				.map(importDto -> this.convert(symbolEntity, importDto, currencyMap)).collect(Collectors.toList());
		return quotes;
	}

	private DailyQuote convert(Symbol symbolEntity, HkDailyQuoteImportDto importDto,
			Map<LocalDate, Collection<Currency>> currencyMap) {
//		Optional<CurrencyKey> currencyOpt = currencyMap.get(importDto.getDate()) == null ? Optional.empty()
//				: currencyMap.get(importDto.getDate()).stream()
//						.filter(entity -> Optional.ofNullable(entity)
//								.filter(myEntity -> myEntity.getFromCurrKey().equals(symbolEntity.getCurrencyKey()))
//								.isPresent())
//						.flatMap(entity -> Stream.of(entity.getFromCurrKey())).findFirst();
//		LOGGER.info(importDto.toString());
		DailyQuote entity = new DailyQuote(null, symbolEntity.getSymbol(), importDto.getOpen(), importDto.getHigh(),
				importDto.getLow(), importDto.getAdjClose(),
				importDto.getVolume() == null ? null : importDto.getVolume().longValue(), importDto.getDate(),
				symbolEntity, symbolEntity.getCurrencyKey());
		return entity;
	}

	private List<DailyQuote> alphavantageImport(String symbol, Map<LocalDate, Collection<Currency>> currencyMap,
			Symbol symbolEntity, List<DailyQuote> entities, Duration delay) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		final Duration myTimeout = Duration.ofSeconds(myDelay.get(ChronoUnit.SECONDS) + 10);
		return entities.isEmpty()
				? this.alphavatageClient.getTimeseriesDailyHistory(symbol, true).delayElement(myDelay)
						.blockOptional(myTimeout).map(wrapper -> this.convert(symbolEntity, wrapper, currencyMap))
						.orElse(List.of())
				: this.alphavatageClient.getTimeseriesDailyHistory(symbol, false).delayElement(myDelay)
						.blockOptional(myTimeout).map(wrapper -> this.convert(symbolEntity, wrapper, currencyMap))
						.orElse(List.of()).stream()
						.filter(dto -> entities.stream()
								.noneMatch(myEntity -> myEntity.getLocalDay().isEqual(dto.getLocalDay())))
						.collect(Collectors.toList());
	}

	private List<IntraDayQuote> convert(Symbol symbolEntity, IntraDayWrapperImportDto wrapper) {
		List<IntraDayQuote> quotes = wrapper.getDailyQuotes().entrySet().stream()
				.map(entry -> this.convert(symbolEntity, entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		return quotes;
	}

	private IntraDayQuote convert(Symbol symbolEntity, String dateStr, IntraDayQuoteImportDto dto) {
		IntraDayQuote entity = new IntraDayQuote(null, symbolEntity.getSymbol(), new BigDecimal(dto.getOpen()),
				new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()), new BigDecimal(dto.getClose()),
				Long.parseLong(dto.getVolume()),
				LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), symbolEntity);
		return entity;
	}

	private List<IntraDayQuote> saveAllIntraDayQuotes(Collection<IntraDayQuote> entities) {
		LOGGER.info("importIntraDayQuotes() {} to import", entities.size());
		return this.intraDayQuoteRepository.saveAll(entities);
	}

	private List<DailyQuote> convert(Symbol symbolEntity, DailyWrapperImportDto wrapper,
			Map<LocalDate, Collection<Currency>> currencyMap) {
		List<DailyQuote> quotes = wrapper.getDailyQuotes().entrySet().stream()
				.map(entry -> this.convert(symbolEntity, entry.getKey(), entry.getValue(), currencyMap))
				.collect(Collectors.toList());
		return quotes;
	}

	private DailyQuote convert(Symbol symbolEntity, String dateStr, DailyQuoteImportDto dto,
			Map<LocalDate, Collection<Currency>> currencyMap) {
//		Optional<CurrencyKey> currencyIdOpt = currencyMap
//				.get(LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)) == null
//						? Optional.empty()
//						: currencyMap.get(LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)).stream()
//								.filter(entity -> Optional.ofNullable(entity.getToCurrKey())
//										.filter(myCurrKey -> myCurrKey.equals(symbolEntity.getCurrencyKey()))
//										.isPresent())
//								.flatMap(entity -> Stream.of(entity.getToCurrKey())).findFirst();
		DailyQuote entity = new DailyQuote(null, symbolEntity.getSymbol(), new BigDecimal(dto.getOpen()),
				new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()), new BigDecimal(dto.getClose()),
				Long.parseLong(dto.getVolume()), LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE),
				symbolEntity, symbolEntity.getCurrencyKey());
		return entity;
	}

	private List<DailyQuote> saveAllDailyQuotes(List<DailyQuote> entities) {
		LOGGER.info("importDailyQuotes() {} to import", entities.size());
		return this.dailyQuoteRepository.saveAll(entities);
	}

	private Long deleteIntraDayQuotes(List<IntraDayQuote> entities) {
		LOGGER.info("deleteIntraDayQuotes() {} to delete", entities.size());
		this.intraDayQuoteRepository.deleteAll(entities);
		return Integer.valueOf(entities.size()).longValue();
	}
}
