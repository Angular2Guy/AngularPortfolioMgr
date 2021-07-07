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
package ch.xxx.manager.contoller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.dto.QuoteDto;
import ch.xxx.manager.service.ComparisonIndex;
import ch.xxx.manager.service.PortfolioToIndexService;
import ch.xxx.manager.service.QuoteImportService;
import ch.xxx.manager.service.QuoteService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("rest/quote")
public class QuoteController {
	@Autowired
	private QuoteService quoteService;
	@Autowired
	private QuoteImportService quoteImportService;
	@Autowired
	private PortfolioToIndexService portfolioToIndexService;

//	@GetMapping("/daily/all/symbol/{symbol}")
//	public Flux<QuoteDto> getAllDailyQuotes(@PathVariable("symbol") String symbol) {
//		return this.quoteService.getDailyQuotes(symbol);
//	}
//
//	@GetMapping("/daily/all/portfolio/{portfolioId}/index/{indexSymbol}")
//	public Flux<QuoteDto> getAllDailyComparisonIndexQuotes(@PathVariable("portfolioId") Long portfolioId,
//			@PathVariable("indexSymbol") String indexSymbol) {
//		ComparisonIndex comparisonIndex = List.of(ComparisonIndex.values()).stream()
//				.filter(value -> value.getSymbol().equals(indexSymbol)).findFirst().orElseThrow();
//		return this.portfolioToIndexService.calculateIndexComparison(portfolioId, comparisonIndex);
//	}
//
//	@GetMapping("/intraday/symbol/{symbol}")
//	public Flux<QuoteDto> getIntraDayQuotes(@PathVariable("symbol") String symbol) {
//		return this.quoteService.getIntraDayQuotes(symbol);
//	}
//
//	@GetMapping("/daily/symbol/{symbol}/start/{start}/end/{end}")
//	public Flux<QuoteDto> getDailyQuotesFromStartToEnd(@PathVariable("symbol") String symbol,
//			@PathVariable("start") String isodateStart, @PathVariable("end") String isodateEnd) {
//		LocalDate start = LocalDate.parse(isodateStart, DateTimeFormatter.ISO_DATE);
//		LocalDate end = LocalDate.parse(isodateEnd, DateTimeFormatter.ISO_DATE);
//		return this.quoteService.getDailyQuotes(symbol, start, end);
//	}
//
//	@GetMapping("/daily/portfolio/{portfolioId}/index/{indexSymbol}/start/{start}/end/{end}")
//	public Flux<QuoteDto> getAllDailyComparisonIndexQuotesFromStartToEnd(@PathVariable("portfolioId") Long portfolioId,
//			@PathVariable("indexSymbol") String indexSymbol, @PathVariable("start") String isodateStart,
//			@PathVariable("end") String isodateEnd) {
//		ComparisonIndex comparisonIndex = List.of(ComparisonIndex.values()).stream()
//				.filter(value -> value.getSymbol().equals(indexSymbol)).findFirst().orElseThrow();
//		LocalDate start = LocalDate.parse(isodateStart, DateTimeFormatter.ISO_DATE);
//		LocalDate end = LocalDate.parse(isodateEnd, DateTimeFormatter.ISO_DATE);
//		return this.portfolioToIndexService.calculateIndexComparison(portfolioId, comparisonIndex, start, end);
//	}
//
//	@GetMapping("/import/daily/symbol/{symbol}")
//	public Mono<Long> importDailyQuotes(@PathVariable("symbol") String symbol) {
//		return this.quoteImportService.importDailyQuoteHistory(symbol);
//	}
//
//	@GetMapping("/import/intraday/symbol/{symbol}")
//	public Mono<Long> importIntraDayQuotes(@PathVariable("symbol") String symbol) {
//		return this.quoteImportService.importIntraDayQuotes(symbol);
//	}
//
//	@GetMapping("/import/daily/currency/{to_curr}")
//	public Mono<Long> importFxDailyQuotes(@PathVariable("to_curr") String to_curr) {
//		return this.quoteImportService.importFxDailyQuoteHistory(to_curr);
//	}
}
