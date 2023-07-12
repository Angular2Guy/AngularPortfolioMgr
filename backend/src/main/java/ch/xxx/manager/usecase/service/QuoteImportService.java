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
import ch.xxx.manager.domain.model.dto.DailyQuoteImportAdjDto;
import ch.xxx.manager.domain.model.dto.DailyWrapperImportDto;
import ch.xxx.manager.domain.model.dto.HkDailyQuoteImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayMetaDataImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayQuoteImportDto;
import ch.xxx.manager.domain.model.dto.IntraDayWrapperImportDto;
import ch.xxx.manager.domain.model.dto.RapidOverviewImportDto;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.IntraDayQuote;
import ch.xxx.manager.domain.model.entity.IntraDayQuoteRepository;
import ch.xxx.manager.domain.model.entity.Sector;
import ch.xxx.manager.domain.model.entity.SectorRepository;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.SymbolRepository;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class QuoteImportService {
	public record UserKeys(String alphavantageKey, String RapidApiKey) {
	}
		
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteImportService.class);
	private final AlphavatageClient alphavatageClient;
	private final YahooClient yahooClient;
	private final RapidApiClient rapidApiClient;
	private final DailyQuoteRepository dailyQuoteRepository;
	private final IntraDayQuoteRepository intraDayQuoteRepository;
	private final SymbolRepository symbolRepository;
	private final CurrencyService currencyService;
	private final SectorRepository sectorRepository;	

	public QuoteImportService(AlphavatageClient alphavatageConnector, YahooClient yahooConnector,
			DailyQuoteRepository dailyQuoteRepository, IntraDayQuoteRepository intraDayQuoteRepository,
			SymbolRepository symbolRepository, CurrencyService currencyService, RapidApiClient rapidApiClient,
			SectorRepository sectorRepository, AppUserRepository appUserRepository) {
		this.alphavatageClient = alphavatageConnector;
		this.yahooClient = yahooConnector;
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.intraDayQuoteRepository = intraDayQuoteRepository;
		this.symbolRepository = symbolRepository;
		this.currencyService = currencyService;
		this.rapidApiClient = rapidApiClient;
		this.sectorRepository = sectorRepository;
	}
	
	public Long importIntraDayQuotes(String symbol, UserKeys userKeys) {
		return this.importIntraDayQuotes(symbol, null, userKeys);
	}

	public Long importIntraDayQuotes(String symbol, Duration delay, UserKeys userKeys) {
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
						this.alphavatageClient.getTimeseriesIntraDay(mySymbol.getSymbol(), userKeys).delayElement(myDelay)
								.blockOptional(myTimeout).orElse(intraDayWrapperImportDto)))
				.peek(myRecord -> this
						.deleteIntraDayQuotes(this.intraDayQuoteRepository.findBySymbol(myRecord.symbol.getSymbol())))
				.map(myRecord -> this.convert(myRecord.symbol, myRecord.intraDayWrapperImportDto))
				.map(myQuotes -> this.saveAllIntraDayQuotes(myQuotes)).count();
	}

	public Long importDailyQuoteHistory(String symbol, UserKeys userKeys) {
		LOGGER.info("importQuoteHistory() called for symbol: {}", symbol);		
		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase()).stream()
				.map(mySymbolEntity -> this.symbolRepository
						.save(this.overviewImport(symbol, mySymbolEntity, Duration.ofSeconds(1L))))
				.flatMap(symbolEntity -> Stream
						.of(this.customImport(symbol, this.currencyService.getCurrencyMap(), symbolEntity, null, userKeys))
						.flatMap(value -> Stream.of(this.saveAllDailyQuotes(value))))
				.count();
	}

	private List<DailyQuote> customImport(String symbol, Map<LocalDate, Collection<Currency>> currencyMap,
			Symbol symbolEntity, Duration delay, UserKeys userKeys) {
		List<DailyQuote> result = switch (symbolEntity.getQuoteSource()) {
		case ALPHAVANTAGE -> this.alphavantageImport(symbol, currencyMap, symbolEntity, delay, userKeys);
		case YAHOO -> this.yahooImport(symbol, currencyMap, symbolEntity, delay);
		default -> List.of();
		};
		LOGGER.info("{} Dailyquotes for Symbol {} imported", result.size(), symbol);
		return result;
	}

	public Long importUpdateDailyQuotes(String symbol, UserKeys userKeys) {
		LOGGER.info("importNewDailyQuotes() called for symbol: {}", symbol);		
		return this.importUpdateDailyQuotes(Set.of(symbol), null, userKeys);
	}

	public Long importUpdateDailyQuotes(String symbol, Duration delay, UserKeys userKeys) {
		LOGGER.info("importNewDailyQuotes() called for symbol: {}", symbol);
		return this.importUpdateDailyQuotes(Set.of(symbol), delay, userKeys);
	}

	public Long importUpdateDailyQuotes(Set<String> symbols, Duration delay, UserKeys userKeys) {
//		LOGGER.info("findBySymbolSingle result: {}", this.symbolRepository.findBySymbolSingle(symbols.iterator().next().toLowerCase()).size());
//		LOGGER.info("count: {}",this.symbolRepository.findBySymbolSingle(symbols.iterator().next().toLowerCase()).stream()
//				.flatMap(mySymbol -> this.customImport(symbols.iterator().next().toLowerCase(), this.currencyService.getCurrencyMap(),
//						mySymbol, delay).stream()).count());
		return symbols.stream()
				.flatMap(symbol -> Stream.of(this.symbolRepository.findBySymbolSingle(symbol.toLowerCase()).stream()
						.flatMap(mySymbol -> Stream.of(this.customImport(symbols.iterator().next().toLowerCase(),
								this.currencyService.getCurrencyMap(), mySymbol, delay, userKeys)))
						.map(values -> this.saveAllDailyQuotes(values)).count()))
				.reduce(0L, (a, b) -> a + b);
	}

	private Symbol overviewImport(String symbol, Symbol symbolEntity, Duration delay) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		final Duration myTimeout = Duration.ofSeconds(myDelay.get(ChronoUnit.SECONDS) + 10);
		final Symbol mySymbolEntity = symbolEntity;
		symbolEntity = switch (mySymbolEntity.getQuoteSource()) {
		case ALPHAVANTAGE ->
			this.alphavatageClient.importCompanyProfile(symbol).delayElement(myDelay).retry(1L).blockOptional(myTimeout)
					.stream().map(myDto -> this.updateSymbol(myDto, mySymbolEntity)).findFirst().orElse(mySymbolEntity);
		case YAHOO ->
			this.rapidApiClient.importCompanyProfile(symbol).delayElement(myDelay).retry(1L).blockOptional(myTimeout)
					.stream().map(myDto -> this.updateSymbol(myDto, mySymbolEntity)).findFirst().orElse(mySymbolEntity);
		default -> Optional.of(mySymbolEntity).get();
		};
		return symbolEntity;
	}

	private Symbol updateSymbol(RapidOverviewImportDto dto, Symbol symbol) {
		LOGGER.info("RapidOverviewImportDto {}", dto);
		symbol.setAddress(String.format("%s %s %s", dto.getAssetProfile().getAddress1(),
				dto.getAssetProfile().getAddress2(), dto.getAssetProfile().getCity()));
		symbol.setCountry(dto.getAssetProfile().getCountry());
		symbol.setDescription(dto.getAssetProfile().getLongBusinessSummary());
		symbol.setIndustry(dto.getAssetProfile().getIndustry());
		symbol.setSectorStr(dto.getAssetProfile().getSector());
		Sector sector = new Sector();
		sector.setYahooName(dto.getAssetProfile().getSector());
		sector.getSymbols().add(symbol);
		sector = this.sectorRepository.save(sector);
		symbol.setSector(sector);
		return symbol;
	}

	private Symbol updateSymbol(AlphaOverviewImportDto dto, Symbol symbol) {
		LOGGER.info("AlphaOverviewImportDto {}", dto);
		symbol.setAddress(dto.getAddress());
		symbol.setCountry(dto.getCountry());
		symbol.setDescription(dto.getDescription());
		symbol.setIndustry(dto.getIndustry());
		symbol.setSectorStr(dto.getSector());
		Sector sector = new Sector();
		sector.setAlphavantageName(dto.getSector());
		sector.getSymbols().add(symbol);
		sector = this.sectorRepository.save(sector);
		symbol.setSector(sector);
		return symbol;
	}

	private List<DailyQuote> yahooImport(String symbol, Map<LocalDate, Collection<Currency>> currencyMap,
			Symbol symbolEntity, Duration delay) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		final Duration myTimeout = Duration.ofSeconds(myDelay.get(ChronoUnit.SECONDS) + 10);
		return symbolEntity.getDailyQuotes() == null || symbolEntity.getDailyQuotes().isEmpty()
				? this.yahooClient.getTimeseriesDailyHistory(symbol).delayElement(myDelay).blockOptional(myTimeout)
						.map(importDtos -> this.convert(symbolEntity, importDtos, currencyMap)).orElse(List.of())
				: this.yahooClient.getTimeseriesDailyHistory(symbol).delayElement(myDelay).blockOptional(myTimeout)
						.map(importDtos -> this.convert(symbolEntity, importDtos, currencyMap)).orElse(List.of())
						.stream()
						.filter(dto -> symbolEntity.getDailyQuotes().stream()
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
		DailyQuote entity = new DailyQuote(null, symbolEntity.getSymbol(), importDto.getOpen(), importDto.getHigh(),
				importDto.getLow(), importDto.getClose(), importDto.getAdjClose(),
				importDto.getVolume() == null ? null : importDto.getVolume().longValue(), importDto.getDate(),
				symbolEntity, symbolEntity.getCurrencyKey());
		symbolEntity.getDailyQuotes().add(entity);
		return entity;
	}

	private List<DailyQuote> alphavantageImport(String symbol, Map<LocalDate, Collection<Currency>> currencyMap,
			Symbol symbolEntity, Duration delay, UserKeys userKeys) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		final Duration myTimeout = Duration.ofSeconds(myDelay.get(ChronoUnit.SECONDS) + 10);
		return this.alphavatageClient.getTimeseriesDailyHistory(symbol, true, userKeys).delayElement(myDelay).retry(1)
				.blockOptional(myTimeout).map(wrapper -> this.convert(symbolEntity, wrapper, currencyMap))
				.orElse(List.of());
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
				LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), symbolEntity,
				symbolEntity.getCurrencyKey());
		symbolEntity.getIntraDayQuotes().add(entity);
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

	private DailyQuote convert(Symbol symbolEntity, String dateStr, DailyQuoteImportAdjDto dto,
			Map<LocalDate, Collection<Currency>> currencyMap) {
		DailyQuote entity = new DailyQuote(null, symbolEntity.getSymbol(), new BigDecimal(dto.getOpen()),
				new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()), new BigDecimal(dto.getClose()),
				new BigDecimal(dto.getAdjustedClose()), Long.parseLong(dto.getVolume()),
				LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE), symbolEntity,
				symbolEntity.getCurrencyKey());
		symbolEntity.getDailyQuotes().add(entity);
		return entity;
	}

	private List<DailyQuote> saveAllDailyQuotes(List<DailyQuote> entities) {
		LOGGER.info("importDailyQuotes() {} to import", entities.size());
		if (entities != null && !entities.isEmpty()) {
			this.dailyQuoteRepository.deleteAll(this.dailyQuoteRepository.findBySymbol(entities.get(0).getSymbolKey()));
		}
		return this.dailyQuoteRepository.saveAll(entities);
	}

	private Long deleteIntraDayQuotes(List<IntraDayQuote> entities) {
		LOGGER.info("deleteIntraDayQuotes() {} to delete", entities.size());
		this.intraDayQuoteRepository.deleteAll(entities);
		return Integer.valueOf(entities.size()).longValue();
	}
}
