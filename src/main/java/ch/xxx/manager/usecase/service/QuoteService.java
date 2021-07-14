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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.adapter.repository.JpaDailyQuoteRepository;
import ch.xxx.manager.adapter.repository.JpaIntraDayQuoteRepository;

@Service
@Transactional
public class QuoteService {
	private final JpaDailyQuoteRepository dailyQuoteRepository;
	private final JpaIntraDayQuoteRepository intraDayQuoteRepository;
	
	public QuoteService(JpaDailyQuoteRepository dailyQuoteRepository, JpaIntraDayQuoteRepository intraDayQuoteRepository) {
		this.dailyQuoteRepository = dailyQuoteRepository;
		this.intraDayQuoteRepository = intraDayQuoteRepository;
	}
	
//	public Flux<QuoteDto> getDailyQuotes(String symbol) {
//		return this.dailyQuoteRepository.findBySymbol(symbol).flatMapSequential(quote -> convert(quote));
//	}
//	
//	public Flux<QuoteDto> getDailyQuotes(String symbol, LocalDate start, LocalDate end) {
//		return this.dailyQuoteRepository.findBySymbolAndDayBetween(symbol, start, end).flatMapSequential(quote -> convert(quote));
//	}
//	
//	public Flux<QuoteDto> getIntraDayQuotes(String symbol) {
//		return this.intraDayQuoteRepository.findBySymbol(symbol).flatMapSequential(quote -> convert(quote));
//	}
//	
//	private Flux<QuoteDto> convert(IntraDayQuote entity) {
//		return Flux.just(new QuoteDto(entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose(), entity.getVolume(), entity.getLocalDateTime(), entity.getSymbol()));
//	}
//	
//	private Flux<QuoteDto> convert(DailyQuote entity) {
//		return Flux.just(new QuoteDto(entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose(), entity.getVolume(), entity.getLocalDay().atStartOfDay(), entity.getSymbol()));
//	}
}
