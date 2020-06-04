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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.connector.AlphavatageConnector;
import ch.xxx.manager.dto.DailyFxQuoteImportDto;
import ch.xxx.manager.dto.DailyFxWrapperImportDto;
import ch.xxx.manager.dto.DailyQuoteImportDto;
import ch.xxx.manager.dto.DailyWrapperImportDto;
import ch.xxx.manager.dto.IntraDayQuoteImportDto;
import ch.xxx.manager.dto.IntraDayWrapperImportDto;
import ch.xxx.manager.entity.CurrencyEntity;
import ch.xxx.manager.entity.DailyQuoteEntity;
import ch.xxx.manager.entity.IntraDayQuoteEntity;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.entity.SymbolEntity.SymbolCurrency;
import ch.xxx.manager.repository.CurrencyRepository;
import ch.xxx.manager.repository.DailyQuoteRepository;
import ch.xxx.manager.repository.IntraDayQuoteRepository;
import ch.xxx.manager.repository.SymbolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class QuoteImportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteImportService.class);
	@Autowired
	private AlphavatageConnector alphavatageController;
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

	public Mono<Long> importIntraDayQuotes(String symbol) {
		LOGGER.info("importIntraDayQuotes() called");
		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase())
				.flatMap(symbolEntity -> this.alphavatageController.getTimeseriesIntraDay(symbol)
						.flatMap(wrapper -> this.convert(symbolEntity, wrapper))
						.flatMapMany(values -> this.saveAllIntraDayQuotes(values)).count()
						.doAfterTerminate(() -> this.intraDayQuoteRepository.findBySymbolId(symbolEntity.getId())
								.groupBy(intraDayQuote -> intraDayQuote.getLocalDateTime().toLocalDate())
								.flatMap(group -> group.collectList())
								.map(groupList -> this.createIntraDayQuoteMap(groupList))
								.map(quotesMap -> this.deleteIntraDayQuotes(this.filterNewestDay(quotesMap)))
								.subscribe()));
	}

	private List<IntraDayQuoteEntity> filterNewestDay(Map<LocalDate, List<IntraDayQuoteEntity>> quotesMap) {
		LocalDate max = quotesMap.keySet().stream().max(LocalDate::compareTo).get();
		quotesMap.remove(max);
		return quotesMap.values().stream().reduce(new ArrayList<IntraDayQuoteEntity>(), (list, entityList) -> {
			list.addAll(entityList);
			return list;
		});

	}

	private Map<LocalDate, List<IntraDayQuoteEntity>> createIntraDayQuoteMap(List<IntraDayQuoteEntity> entities) {
		Map<LocalDate, List<IntraDayQuoteEntity>> myMap = new HashMap<>();
		myMap.put(entities.get(0).getLocalDateTime().toLocalDate(), entities);
		return myMap;
	}

	public Mono<Long> importDailyQuoteHistory(String symbol) {
		LOGGER.info("importQuoteHistory() called");
		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase())
				.flatMap(symbolEntity -> this.alphavatageController.getTimeseriesDailyHistory(symbol, true)
						.flatMap(wrapper -> this.convert(symbolEntity, wrapper))
						.flatMapMany(value -> this.saveAllDailyQuotes(value)).count());
	}

	public Mono<Long> importUpdateDailyQuotes(String symbol) {
		LOGGER.info("importNewDailyQuotes() called");
		return this.symbolRepository.findBySymbolSingle(symbol.toLowerCase())
				.flatMap(symbolEntity -> this.dailyQuoteRepository.findBySymbolId(symbolEntity.getId()).collectList()
						.flatMap(entities -> entities.isEmpty()
								? this.alphavatageController.getTimeseriesDailyHistory(symbol, true)
										.flatMap(wrapper -> this.convert(symbolEntity, wrapper))
								: this.alphavatageController.getTimeseriesDailyHistory(symbol, false)
										.flatMap(wrapper -> this.convert(symbolEntity, wrapper))
										.map(dtos -> dtos.stream()
												.filter(myDto -> 1 > entities.get(entities.size() - 1).getLocalDay()
														.compareTo(myDto.getLocalDay()))
												.collect(Collectors.toList())))
						.flatMapMany(value -> this.saveAllDailyQuotes(value)).count());
	}

	public Mono<Long> importFxDailyQuoteHistory(String to_currency) {
		LOGGER.info("importFxDailyQuoteHistory() called");
		return this.alphavatageController.getFxTimeseriesDailyHistory(to_currency, true)
				.flatMap(wrapper -> this.currencyRepository.saveAll(this.convert(wrapper)).count());
	}

	private List<CurrencyEntity> convert(DailyFxWrapperImportDto wrapperDto) {
		return wrapperDto.getDailyQuotes().entrySet().stream()
				.flatMap(entry -> Stream.of(this.convert(entry, SymbolCurrency.valueOf(wrapperDto.getMetadata().getFromSymbol()), 
						SymbolCurrency.valueOf(wrapperDto.getMetadata().getToSymbol()))))
						.collect(Collectors.toList());
	}

	private CurrencyEntity convert(Entry<String, DailyFxQuoteImportDto> entry, SymbolCurrency from_curr,
			SymbolCurrency to_curr) {
		return new CurrencyEntity(
				LocalDate.parse(entry.getKey(), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
				from_curr.toString(), to_curr.toString(), new BigDecimal(entry.getValue().getOpen()),
				new BigDecimal(entry.getValue().getHigh()), new BigDecimal(entry.getValue().getLow()),
				new BigDecimal(entry.getValue().getClose()));
	}

	private Mono<List<IntraDayQuoteEntity>> convert(SymbolEntity symbolEntity, IntraDayWrapperImportDto wrapper) {
		List<IntraDayQuoteEntity> quotes = wrapper.getDailyQuotes().entrySet().stream()
				.map(entry -> this.convert(symbolEntity, entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		return Mono.just(quotes);
	}

	private IntraDayQuoteEntity convert(SymbolEntity symbolEntity, String dateStr, IntraDayQuoteImportDto dto) {
		IntraDayQuoteEntity entity = new IntraDayQuoteEntity(null, symbolEntity.getSymbol(),
				new BigDecimal(dto.getOpen()), new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()),
				new BigDecimal(dto.getClose()), Long.parseLong(dto.getVolume()),
				LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), symbolEntity.getId());
		return entity;
	}

	private Flux<IntraDayQuoteEntity> saveAllIntraDayQuotes(Collection<IntraDayQuoteEntity> entities) {
		LOGGER.info("importIntraDayQuotes() {} to import", entities.size());
		return this.intraDayQuoteRepository.saveAll(entities);
	}

	private Mono<List<DailyQuoteEntity>> convert(SymbolEntity symbolEntity, DailyWrapperImportDto wrapper) {
		List<DailyQuoteEntity> quotes = wrapper.getDailyQuotes().entrySet().stream()
				.map(entry -> this.convert(symbolEntity, entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		return Mono.just(quotes);
	}

	private DailyQuoteEntity convert(SymbolEntity symbolEntity, String dateStr, DailyQuoteImportDto dto) {
		DailyQuoteEntity entity = new DailyQuoteEntity(null, symbolEntity.getSymbol(), new BigDecimal(dto.getOpen()),
				new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()), new BigDecimal(dto.getAjustedClose()),
				Long.parseLong(dto.getVolume()), LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE),
				symbolEntity.getId());
		return entity;
	}

	private Flux<DailyQuoteEntity> saveAllDailyQuotes(Collection<DailyQuoteEntity> entities) {
		LOGGER.info("importDailyQuotes() {} to import", entities.size());
		return this.dailyQuoteRepository.saveAll(entities);
	}

	private Mono<Void> deleteIntraDayQuotes(Collection<IntraDayQuoteEntity> entities) {
		LOGGER.info("deleteIntraDayQuotes() {} to delete", entities.size());
		return !entities.isEmpty() ? this.intraDayQuoteRepository.deleteAll(entities) : Mono.empty();
	}
}
