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
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.xxx.manager.contoller.AlphavatageConnector;
import ch.xxx.manager.dto.DailyQuoteImportDto;
import ch.xxx.manager.dto.DailyWrapperImportDto;
import ch.xxx.manager.entity.DailyQuoteEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class QuoteImportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteImportService.class);
	@Autowired
	private AlphavatageConnector alphavatageController;
	@Autowired
	private DailyQuoteRepository dailyQuoteRepository;

	public Mono<Long> importQuoteHistory(String symbol) {
		LOGGER.info("importQuoteHistory() called");		
		return this.alphavatageController.getTimeseriesHistory(symbol)
			.flatMap(wrapper -> this.convert(symbol, wrapper))
			.flatMapMany(value -> this.saveAll(value)).count();
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

	private Flux<DailyQuoteEntity> saveAll(Collection<DailyQuoteEntity> entities) {
		LOGGER.info("importQuoteHistory() {} to import", entities.size());
		return this.dailyQuoteRepository.saveAll(entities);		
		
	}
}
