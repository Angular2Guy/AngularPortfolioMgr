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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.contoller.AlphavatageConnector;
import ch.xxx.manager.dto.DailyQuoteImportDto;
import ch.xxx.manager.dto.DailyWrapperImportDto;
import ch.xxx.manager.dto.IntraDayQuoteImportDto;
import ch.xxx.manager.dto.IntraDayWrapperImportDto;
import ch.xxx.manager.entity.DailyQuoteEntity;
import ch.xxx.manager.entity.IntraDayQuoteEntity;
import ch.xxx.manager.repository.DailyQuoteRepository;
import ch.xxx.manager.repository.IntraDayQuoteRepository;
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
	
	@Scheduled(cron = "0 0 2 * * ?")
	public void scheduledImporter() {
		
	}
	
	public Mono<Long> importIntraDayQuotes(String symbol) {
		LOGGER.info("importIntraDayQuotes() called");			
		return this.alphavatageController.getTimeseriesIntraDay(symbol)
				.flatMap(wrapper -> this.convert(symbol, wrapper))
				.flatMapMany(values -> this.saveAllIntraDayQuotes(values)).count()
				.doAfterTerminate(() -> 
					this.intraDayQuoteRepository.findBySymbol(symbol)
					.collectList().map(oldQuotes -> this.deleteIntraDayQuotes(oldQuotes))
					.subscribe());
	}
	
	public Mono<Long> importDailyQuoteHistory(String symbol) {
		LOGGER.info("importQuoteHistory() called");		
		return this.alphavatageController.getTimeseriesDailyHistory(symbol, true)
			.flatMap(wrapper -> this.convert(symbol, wrapper))
			.flatMapMany(value -> this.saveAllDailyQuotes(value)).count();
	}

	public Mono<Long> importUpdateDailyQuotes(String symbol) {
		LOGGER.info("importNewDailyQuotes() called");
		return this.dailyQuoteRepository.findBySymbol(symbol).collectList()
			.flatMap(entities -> entities.isEmpty() ? 
					this.alphavatageController.getTimeseriesDailyHistory(symbol, true)
						.flatMap(wrapper -> this.convert(symbol, wrapper)) 
					: this.alphavatageController.getTimeseriesDailyHistory(symbol, false)
						.flatMap(wrapper -> this.convert(symbol, wrapper))
						.map(dtos -> dtos.stream().filter(myDto -> 1 > entities.get(entities.size()-1).getDay().compareTo(myDto.getDay())).collect(Collectors.toList()))).
			flatMapMany(value -> this.saveAllDailyQuotes(value)).count();
	}
	
	private Mono<List<IntraDayQuoteEntity>> convert(String symbol, IntraDayWrapperImportDto wrapper) {
		List<IntraDayQuoteEntity> quotes = wrapper.getDailyQuotes().entrySet().stream()
				.map(entry -> this.convert(symbol, entry.getKey(), entry.getValue())).collect(Collectors.toList());
		return Mono.just(quotes);
	}

	private IntraDayQuoteEntity convert(String symbol, String dateStr, IntraDayQuoteImportDto dto) {
		IntraDayQuoteEntity entity = new IntraDayQuoteEntity(null, symbol, new BigDecimal(dto.getOpen()),
				new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()), new BigDecimal(dto.getClose()), 
				Long.parseLong(dto.getVolume()), LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		return entity;
	}

	private Flux<IntraDayQuoteEntity> saveAllIntraDayQuotes(Collection<IntraDayQuoteEntity> entities) {
		LOGGER.info("importDailyQuotes() {} to import", entities.size());
		return this.intraDayQuoteRepository.saveAll(entities);				
	}
	
	private Mono<List<DailyQuoteEntity>> convert(String symbol, DailyWrapperImportDto wrapper) {
		List<DailyQuoteEntity> quotes = wrapper.getDailyQuotes().entrySet().stream()
				.map(entry -> this.convert(symbol, entry.getKey(), entry.getValue())).collect(Collectors.toList());
		return Mono.just(quotes);
	}

	private DailyQuoteEntity convert(String symbol, String dateStr, DailyQuoteImportDto dto) {
		DailyQuoteEntity entity = new DailyQuoteEntity(null, symbol, new BigDecimal(dto.getOpen()),
				new BigDecimal(dto.getHigh()), new BigDecimal(dto.getLow()), new BigDecimal(dto.getAjustedClose()),
				Long.parseLong(dto.getVolume()), LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE));
		return entity;
	}

	private Flux<DailyQuoteEntity> saveAllDailyQuotes(Collection<DailyQuoteEntity> entities) {
		LOGGER.info("importDailyQuotes() {} to import", entities.size());
		return this.dailyQuoteRepository.saveAll(entities);				
	}
	
	private Mono<Void> deleteIntraDayQuotes(Collection<IntraDayQuoteEntity> entities) {
		LOGGER.info("deleteIntraDayQuotes() {} to delete",entities.size());
		List<LocalDate> myDates = entities.stream().map(entity -> entity.getLocaldatetime().toLocalDate()).distinct().collect(Collectors.toList());
		List<IntraDayQuoteEntity> toDelete = !myDates.contains(LocalDate.now()) ? new LinkedList<IntraDayQuoteEntity>() : entities.stream().filter(entity -> LocalDate.now().equals(entity.getLocaldatetime().toLocalDate())).collect(Collectors.toList());
		return this.intraDayQuoteRepository.deleteAll(toDelete);
	}
}
