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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.model.dto.QuoteDto;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.IntraDayQuoteRepository;
import ch.xxx.manager.usecase.mapping.QuoteMapper;

@Service
@Transactional
public class QuoteService {
	private final DailyQuoteRepository dailyQuoteRepository;
	private final IntraDayQuoteRepository intraDayQuoteRepository;
	private final QuoteMapper quoteMapper;

	public QuoteService(DailyQuoteRepository dailyQuoteRepository, IntraDayQuoteRepository intraDayQuoteRepository,
			QuoteMapper quoteMapper) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.intraDayQuoteRepository = intraDayQuoteRepository;
		this.quoteMapper = quoteMapper;
	}

	public List<QuoteDto> getDailyQuotes(String symbol) {
		return this.dailyQuoteRepository.findBySymbol(symbol).stream()
				.flatMap(quote -> Stream.of(this.quoteMapper.convert(quote))).collect(Collectors.toList());
	}

	public List<QuoteDto> getDailyQuotes(String symbol, LocalDate start, LocalDate end) {
		return this.dailyQuoteRepository.findBySymbolAndDayBetween(symbol, start, end).stream()
				.flatMap(quote -> Stream.of(this.quoteMapper.convert(quote))).collect(Collectors.toList());
	}

	public List<QuoteDto> getIntraDayQuotes(String symbol) {
		return this.intraDayQuoteRepository.findBySymbol(symbol).stream()
				.flatMap(quote -> Stream.of(this.quoteMapper.convert(quote))).collect(Collectors.toList());
	}
}
