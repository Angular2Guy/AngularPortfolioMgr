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
package ch.xxx.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.connector.AlphavatageConnector;
import ch.xxx.manager.connector.YahooConnector;
import ch.xxx.manager.repository.CurrencyRepository;
import ch.xxx.manager.repository.DailyQuoteRepository;
import ch.xxx.manager.repository.IntraDayQuoteRepository;
import ch.xxx.manager.repository.SymbolRepository;

@Service
@Transactional
public class QuoteImportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteImportService.class);
	@Autowired
	private AlphavatageConnector alphavatageConnector;
	@Autowired
	private YahooConnector yahooConnector;
	@Autowired
	private DailyQuoteRepository dailyQuoteRepository;
	@Autowired
	private IntraDayQuoteRepository intraDayQuoteRepository;
	@Autowired
	private SymbolRepository symbolRepository;
	@Autowired
	private CurrencyRepository currencyRepository;

	@Scheduled(cron = "0 0 2 * * ?")
	public void scheduledImporter() {

	}

//	public Mono<Long> importIntraDayQuotes(String symbol) {
//		IntraDayWrapperImportDto intraDayWrapperImportDto = new IntraDayWrapperImportDto();
//		intraDayWrapperImportDto.setDailyQuotes(new HashMap<String, IntraDayQuoteImportDto>());
//		intraDayWrapperImportDto.setMetaData(new IntraDayMetaDataImportDto());
//
//		LOGGER.info("importIntraDayQuotes() called for symbol: {}", symbol);
//		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase())
//				.flatMap(symbolEntity -> (QuoteSource.ALPHAVANTAGE.toString().equals(symbolEntity.getSource())
//						? this.alphavatageConnector.getTimeseriesIntraDay(symbol)
//						: Mono.just(intraDayWrapperImportDto)).flatMap(wrapper -> this.convert(symbolEntity, wrapper))
//								.flatMapMany(values -> this.saveAllIntraDayQuotes(values)).count()
//								.doAfterTerminate(() -> this.intraDayQuoteRepository
//										.findBySymbolId(symbolEntity.getId())
//										.groupBy(intraDayQuote -> intraDayQuote.getLocalDateTime().toLocalDate())
//										.flatMap(group -> group.collectList())
//										.map(groupList -> this.createIntraDayQuoteMap(groupList))
//										.map(quotesMap -> this.deleteIntraDayQuotes(this.filterNewestDay(quotesMap)))
//										.subscribe()));
//	}
//
//	private List<IntraDayQuote> filterNewestDay(Map<LocalDate, List<IntraDayQuote>> quotesMap) {
//		LocalDate max = quotesMap.keySet().stream().max(LocalDate::compareTo).get();
//		quotesMap.remove(max);
//		return quotesMap.values().stream().reduce(new ArrayList<IntraDayQuote>(), (list, entityList) -> {
//			list.addAll(entityList);
//			return list;
//		});
//
//	}
//
//	private Map<LocalDate, List<IntraDayQuote>> createIntraDayQuoteMap(List<IntraDayQuote> entities) {
//		Map<LocalDate, List<IntraDayQuote>> myMap = new HashMap<>();
//		myMap.put(entities.get(0).getLocalDateTime().toLocalDate(), entities);
//		return myMap;
//	}
//
//	public Mono<Long> importDailyQuoteHistory(String symbol) {
//		LOGGER.info("importQuoteHistory() called for symbol: {}", symbol);
//		Map<LocalDate, Collection<Currency>> currencyMap = this.createCurrencyMap();
//		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase())
//				.flatMap(symbolEntity -> this.customImport(symbol, currencyMap, symbolEntity, List.of())
//						.flatMapMany(value -> this.saveAllDailyQuotes(value)).count());
//	}
//
//	private Mono<? extends List<DailyQuote>> customImport(String symbol,
//			Map<LocalDate, Collection<Currency>> currencyMap, Symbol symbolEntity,
//			List<DailyQuote> entities) {
//		if (QuoteSource.ALPHAVANTAGE.toString().equals(symbolEntity.getSource())) {
//			return this.alphavantageImport(symbol, currencyMap, symbolEntity, List.of());
//		} else if (QuoteSource.YAHOO.toString().equals(symbolEntity.getSource())) {
//			return this.yahooImport(symbol, currencyMap, symbolEntity, List.of());
//		}
//		return Mono.just(List.of());
//	}
//
//	public Mono<Long> importUpdateDailyQuotes(String symbol) {
//		LOGGER.info("importNewDailyQuotes() called for symbol: {}", symbol);
//		Map<LocalDate, Collection<Currency>> currencyMap = this.createCurrencyMap();
//		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase())
//				.flatMap(symbolEntity -> this.dailyQuoteRepository.findBySymbolId(symbolEntity.getId()).collectList()
//						.flatMap(entities -> this.customImport(symbol, currencyMap, symbolEntity, entities))
//						.flatMapMany(value -> this.saveAllDailyQuotes(value)).count());
//	}
//
//	private Mono<? extends List<DailyQuote>> yahooImport(String symbol,
//			Map<LocalDate, Collection<Currency>> currencyMap, Symbol symbolEntity,
//			List<DailyQuote> entities) {
//		return entities.isEmpty()
//				? this.yahooConnector.getTimeseriesDailyHistory(symbol)
//						.flatMap(importDtos -> this.convert(symbolEntity, importDtos, currencyMap))
//				: this.yahooConnector.getTimeseriesDailyHistory(symbol)
//						.flatMap(
//								importDtos -> this.convert(symbolEntity, importDtos, currencyMap))
//						.map(dtos -> dtos.stream().filter(myDto -> 1 > entities.get(entities.size() - 1).getLocalDay()
//								.compareTo(myDto.getLocalDay())).collect(Collectors.toList()));
//	}
//
//	private Mono<List<DailyQuote>> convert(Symbol symbolEntity, List<HkDailyQuoteImportDto> importDtos,
//			Map<LocalDate, Collection<Currency>> currencyMap) {
//		List<DailyQuote> quotes = importDtos.stream()
//				.filter(myImportDto -> myImportDto.getAdjClose() != null && myImportDto.getVolume() != null)
//				.map(importDto -> this.convert(symbolEntity, importDto, currencyMap)).collect(Collectors.toList());
//		return Mono.just(quotes);
//	}
//
//	private DailyQuote convert(Symbol symbolEntity, HkDailyQuoteImportDto importDto,
//			Map<LocalDate, Collection<Currency>> currencyMap) {
//		Optional<Long> currencyIdOpt = currencyMap.get(importDto.getDate()) == null ? Optional.empty()
//				: currencyMap.get(importDto.getDate()).stream()
//						.filter(entity -> entity.getTo_curr() == null || (entity.getTo_curr() != null
//								&& entity.getTo_curr().equalsIgnoreCase(symbolEntity.getCurr())))
//						.flatMap(entity -> Stream.of(entity.getId())).findFirst();
////		LOGGER.info(importDto.toString());
//		DailyQuote entity = new DailyQuote(null, symbolEntity.getSymbol(), importDto.getOpen(),
//				importDto.getHigh(), importDto.getLow(), importDto.getAdjClose(),
//				importDto.getVolume() == null ? null : importDto.getVolume().longValue(), importDto.getDate(),
//				symbolEntity.getId(), currencyIdOpt.orElse(null));
//		return entity;
//	}
//
//	private Mono<? extends List<DailyQuote>> alphavantageImport(String symbol,
//			Map<LocalDate, Collection<Currency>> currencyMap, Symbol symbolEntity,
//			List<DailyQuote> entities) {
//		return entities.isEmpty()
//				? this.alphavatageConnector.getTimeseriesDailyHistory(symbol, true)
//						.flatMap(wrapper -> this.convert(symbolEntity, wrapper, currencyMap))
//				: this.alphavatageConnector.getTimeseriesDailyHistory(symbol, false)
//						.flatMap(
//								wrapper -> this.convert(symbolEntity, wrapper, currencyMap))
//						.map(dtos -> dtos.stream().filter(myDto -> 1 > entities.get(entities.size() - 1).getLocalDay()
//								.compareTo(myDto.getLocalDay())).collect(Collectors.toList()));
//	}
//
//	public Mono<Long> importFxDailyQuoteHistory(String to_currency) {
//		LOGGER.info("importFxDailyQuoteHistory() called to currency: {}", to_currency);
//		return this.currencyRepository.findAll().collectMultimap(entity -> entity.getLocalDay(), entity -> entity)
//				.flatMap(currencyMap -> this.alphavatageConnector.getFxTimeseriesDailyHistory(to_currency, true)
//						.flatMap(wrapper -> this.currencyRepository.saveAll(this.convert(wrapper, currencyMap))
//								.count()));
//	}
//
//	private Map<LocalDate, Collection<Currency>> createCurrencyMap() {
//		return this.currencyRepository.findAll().collectMultimap(grouped -> grouped.getLocalDay(), grouped -> grouped)
//				.block();
//	}
//
//	private List<Currency> convert(DailyFxWrapperImportDto wrapperDto,
//			Map<LocalDate, Collection<Currency>> currencyMap) {
////		LOGGER.info(wrapperDto.getDailyQuotes().get(LocalDate.of(2014, 12, 29)).getClose());
//		return wrapperDto.getDailyQuotes().entrySet().stream().flatMap(
//				entry -> Stream.of(this.convert(entry, SymbolCurrency.valueOf(wrapperDto.getMetadata().getFromSymbol()),
//						SymbolCurrency.valueOf(wrapperDto.getMetadata().getToSymbol()))))
//				.filter(entity -> currencyMap.get(entity.getLocalDay()) == null
//						|| currencyMap.get(entity.getLocalDay()).stream()
//								.anyMatch(mapEntity -> SymbolCurrency.valueOf(entity.getTo_curr())
//										.equals(SymbolCurrency.valueOf(mapEntity.getTo_curr()))))
//				.collect(Collectors.toList());
//	}
//
//	private Currency convert(Entry<String, DailyFxQuoteImportDto> entry, SymbolCurrency from_curr,
//			SymbolCurrency to_curr) {
//		return new Currency(LocalDate.parse(entry.getKey(), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
//				from_curr.toString(), to_curr.toString(), new BigDecimal(entry.getValue().getOpen()),
//				new BigDecimal(entry.getValue().getHigh()), new BigDecimal(entry.getValue().getLow()),
//				new BigDecimal(entry.getValue().getClose()));
//	}
//
//	private Mono<List<IntraDayQuote>> convert(Symbol symbolEntity, IntraDayWrapperImportDto wrapper) {
//		List<IntraDayQuote> quotes = wrapper.getDailyQuotes().entrySet().stream()
//				.map(entry -> this.convert(symbolEntity, entry.getKey(), entry.getValue()))
//				.collect(Collectors.toList());
//		return Mono.just(quotes);
//	}
//
//	private IntraDayQuote convert(Symbol symbolEntity, String dateStr, IntraDayQuoteImportDto dto) {
//		IntraDayQuote entity = new IntraDayQuote(null, symbolEntity.getSymbol(),
//				new BigDecimal(dto.getOpen()), new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()),
//				new BigDecimal(dto.getClose()), Long.parseLong(dto.getVolume()),
//				LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), symbolEntity.getId());
//		return entity;
//	}
//
//	private Flux<IntraDayQuote> saveAllIntraDayQuotes(Collection<IntraDayQuote> entities) {
//		LOGGER.info("importIntraDayQuotes() {} to import", entities.size());
//		return this.intraDayQuoteRepository.saveAll(entities);
//	}
//
//	private Mono<List<DailyQuote>> convert(Symbol symbolEntity, DailyWrapperImportDto wrapper,
//			Map<LocalDate, Collection<Currency>> currencyMap) {
//		List<DailyQuote> quotes = wrapper.getDailyQuotes().entrySet().stream()
//				.map(entry -> this.convert(symbolEntity, entry.getKey(), entry.getValue(), currencyMap))
//				.collect(Collectors.toList());
//		return Mono.just(quotes);
//	}
//
//	private DailyQuote convert(Symbol symbolEntity, String dateStr, DailyQuoteImportDto dto,
//			Map<LocalDate, Collection<Currency>> currencyMap) {
//		Optional<Long> currencyIdOpt = currencyMap
//				.get(LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)) == null
//						? Optional.empty()
//						: currencyMap.get(LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)).stream()
//								.filter(entity -> entity.getTo_curr() == null || (entity.getTo_curr() != null
//										&& entity.getTo_curr().equalsIgnoreCase(symbolEntity.getCurr())))
//								.flatMap(entity -> Stream.of(entity.getId())).findFirst();
//		DailyQuote entity = new DailyQuote(null, symbolEntity.getSymbol(), new BigDecimal(dto.getOpen()),
//				new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()), new BigDecimal(dto.getAjustedClose()),
//				Long.parseLong(dto.getVolume()), LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE),
//				symbolEntity.getId(), currencyIdOpt.orElse(null));
//		return entity;
//	}
//
//	private Flux<DailyQuote> saveAllDailyQuotes(Collection<DailyQuote> entities) {
//		LOGGER.info("importDailyQuotes() {} to import", entities.size());
//		return this.dailyQuoteRepository.saveAll(entities);
//	}
//
//	private Mono<Void> deleteIntraDayQuotes(Collection<IntraDayQuote> entities) {
//		LOGGER.info("deleteIntraDayQuotes() {} to delete", entities.size());
//		return !entities.isEmpty() ? this.intraDayQuoteRepository.deleteAll(entities) : Mono.empty();
//	}
}
