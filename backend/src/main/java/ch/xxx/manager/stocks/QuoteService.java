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
package ch.xxx.manager.stocks;

import ch.xxx.manager.common.file.FileClient;
import ch.xxx.manager.stocks.dto.ImportDataDto;
import ch.xxx.manager.stocks.dto.QuoteDto;
import ch.xxx.manager.stocks.mapping.QuoteMapper;
import ch.xxx.manager.stocks.repository.JpaDailyQuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class QuoteService {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteService.class);
	private final JpaDailyQuoteRepository dailyQuoteRepository;
	private final QuoteMapper quoteMapper;
	private final FileClient fileClient;

	public QuoteService(JpaDailyQuoteRepository dailyQuoteRepository,
			QuoteMapper quoteMapper, @Qualifier("Stock") FileClient fileClient) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.quoteMapper = quoteMapper;
		this.fileClient = fileClient;
	}

	public List<QuoteDto> getDailyQuotes(String symbol) {
		return this.dailyQuoteRepository.findBySymbol(symbol).stream()
				.flatMap(quote -> Stream.of(this.quoteMapper.convert(quote))).collect(Collectors.toList());
	}

	public List<QuoteDto> getDailyQuotes(String symbol, LocalDate start, LocalDate end) {
		return this.dailyQuoteRepository.findBySymbolAndDayBetween(symbol, start, end).stream()
				.flatMap(quote -> Stream.of(this.quoteMapper.convert(quote))).collect(Collectors.toList());
	}

	@Async
	public void importUsDailyQuotes(ImportDataDto importFinancialDataDto) {
//		LOGGER.info("ImportUsDailyQuotes.");
		this.fileClient.importZipFile(importFinancialDataDto.getFilename());
	}
}
