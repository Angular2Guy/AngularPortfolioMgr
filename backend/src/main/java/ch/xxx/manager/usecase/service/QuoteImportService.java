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

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
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

import ch.xxx.manager.domain.model.dto.RapidOverviewImportDto;
import ch.xxx.manager.domain.model.dto.YahooDailyQuoteImportDto;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.DailyQuote;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.Sector;
import ch.xxx.manager.domain.model.entity.SectorRepository;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.SymbolRepository;
import ch.xxx.manager.domain.utils.DataHelper.CurrencyKey;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class QuoteImportService {
	public record UserKeys(String alphavantageKey, String RapidApiKey) {
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteImportService.class);
	private final YahooClient yahooClient;
	private final RapidApiClient rapidApiClient;
	private final DailyQuoteRepository dailyQuoteRepository;
	private final SymbolRepository symbolRepository;
	private final CurrencyService currencyService;
	private final SectorRepository sectorRepository;

	public QuoteImportService(YahooClient yahooConnector,
			DailyQuoteRepository dailyQuoteRepository, 
			SymbolRepository symbolRepository, CurrencyService currencyService, RapidApiClient rapidApiClient,
			SectorRepository sectorRepository, AppUserRepository appUserRepository) {
		this.yahooClient = yahooConnector;
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.symbolRepository = symbolRepository;
		this.currencyService = currencyService;
		this.rapidApiClient = rapidApiClient;
		this.sectorRepository = sectorRepository;
	}

	public Long importDailyQuoteHistory(String symbol, UserKeys userKeys) {
		LOGGER.info("importQuoteHistory() called for symbol: {}", symbol);
		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase()).stream()
				.map(mySymbolEntity -> this.symbolRepository
						.save(this.overviewImport(symbol, mySymbolEntity, Duration.ofSeconds(1L))))
				.flatMap(symbolEntity -> Stream.of(
						this.customImport(symbol, this.currencyService.getCurrencyMap(), symbolEntity, null, userKeys))
						.flatMap(value -> Stream.of(this.saveAllDailyQuotes(value))))
				.count();
	}

	private List<DailyQuote> customImport(String symbol, Map<LocalDate, Collection<Currency>> currencyMap,
			Symbol symbolEntity, Duration delay, UserKeys userKeys) {
		List<DailyQuote> result = switch (symbolEntity.getQuoteSource()) {
		case ALPHAVANTAGE -> this.yahooImport(symbol, currencyMap, symbolEntity, delay);
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

	public void storeDailyQuoteData(
			List<ch.xxx.manager.domain.model.entity.dto.DailyQuoteImportDto> dailyQuoteImportDtos) {
		Map<String, Symbol> symbolMap = dailyQuoteImportDtos.stream().map(myDto -> myDto.getSymbol()).distinct()
				.map(mySym -> this.symbolRepository.findBySymbol(mySym).stream().findFirst().orElse(
						this.symbolRepository.save(new Symbol(null, mySym, mySym, CurrencyKey.USD, QuoteSource.DATA))))
				.filter(mySymbol -> mySymbol.getQuoteSource().equals(QuoteSource.DATA))
				.collect(Collectors.toMap(mySym -> mySym.getSymbol(), mySym -> mySym));
		this.dailyQuoteRepository.saveAll(this.map(
				dailyQuoteImportDtos.stream()
						.filter(myDto -> Optional.ofNullable(symbolMap.get(myDto.getSymbol())).isPresent()).toList(),
				symbolMap));
	}

	private List<DailyQuote> map(
			Collection<ch.xxx.manager.domain.model.entity.dto.DailyQuoteImportDto> dailyQuoteImportDtos,
			Map<String, Symbol> symbolMap) {
		return dailyQuoteImportDtos.stream()
				.map(myDto -> new DailyQuote(null, myDto.getSymbol(), myDto.getOpen(), myDto.getHigh(), myDto.getLow(),
						myDto.getClose(), myDto.getClose(), 0L, myDto.getDate(), symbolMap.get(myDto.getSymbol()),
						CurrencyKey.USD))
				.toList();
	}

	private Symbol overviewImport(String symbol, Symbol symbolEntity, Duration delay) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		final Symbol mySymbolEntity = symbolEntity;
		try {
			Thread.sleep(myDelay);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		symbolEntity = switch (mySymbolEntity.getQuoteSource()) {
		case ALPHAVANTAGE -> this.rapidApiClient.importCompanyProfile(symbol).stream()
		.map(myDto -> this.updateSymbol(myDto, mySymbolEntity)).findFirst().orElse(mySymbolEntity);
		case YAHOO -> this.rapidApiClient.importCompanyProfile(symbol).stream()
				.map(myDto -> this.updateSymbol(myDto, mySymbolEntity)).findFirst().orElse(mySymbolEntity);
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

	private List<DailyQuote> yahooImport(String symbol, Map<LocalDate, Collection<Currency>> currencyMap,
			Symbol symbolEntity, Duration delay) {
		final Duration myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		try {
			Thread.sleep(myDelay);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return symbolEntity.getDailyQuotes() == null || symbolEntity.getDailyQuotes().isEmpty()
				? this.yahooClient.getTimeseriesDailyHistory(symbol).stream().filter(QuoteImportService::filterEmptyValuesDto)
						.map(importDtos -> this.convert(symbolEntity, importDtos, currencyMap)).toList()
				: this.yahooClient.getTimeseriesDailyHistory(symbol).stream().filter(QuoteImportService::filterEmptyValuesDto)
						.map(importDtos -> this.convert(symbolEntity, importDtos, currencyMap))
						.filter(dto -> symbolEntity.getDailyQuotes().stream()
								.noneMatch(myEntity -> myEntity.getLocalDay().isEqual(dto.getLocalDay())))
						.collect(Collectors.toList());
	}

	private static boolean filterEmptyValuesDto(YahooDailyQuoteImportDto dto) {
		return dto.getAdjClose() != null && dto.getVolume() != null;
	}

	private DailyQuote convert(Symbol symbolEntity, YahooDailyQuoteImportDto importDto,
			Map<LocalDate, Collection<Currency>> currencyMap) {
		DailyQuote entity = new DailyQuote(null, symbolEntity.getSymbol(), importDto.getOpen(), importDto.getHigh(),
				importDto.getLow(), importDto.getClose(), importDto.getAdjClose(),
				importDto.getVolume() == null ? null : importDto.getVolume().longValue(), importDto.getDate(),
				symbolEntity, symbolEntity.getCurrencyKey());
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
}
