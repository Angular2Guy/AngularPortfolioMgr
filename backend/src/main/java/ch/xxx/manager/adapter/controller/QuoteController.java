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
package ch.xxx.manager.adapter.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.domain.exception.AuthenticationException;
import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.dto.ImportDataDto;
import ch.xxx.manager.domain.model.dto.QuoteDto;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.dto.DailyQuoteEntityDto;
import ch.xxx.manager.usecase.service.AppUserService;
import ch.xxx.manager.usecase.service.ComparisonIndex;
import ch.xxx.manager.usecase.service.CurrencyService;
import ch.xxx.manager.usecase.service.JwtTokenService;
import ch.xxx.manager.usecase.service.PortfolioToIndexService;
import ch.xxx.manager.usecase.service.QuoteImportService;
import ch.xxx.manager.usecase.service.QuoteImportService.UserKeys;
import ch.xxx.manager.usecase.service.QuoteService;
import ch.xxx.manager.usecase.service.SymbolImportService;

@RestController
@RequestMapping("rest/quote")
public class QuoteController {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteController.class);
	private final QuoteService quoteService;
	private final QuoteImportService quoteImportService;
	private final PortfolioToIndexService portfolioToIndexService;
	private final CurrencyService currencyService;
	private final AppUserService appUserService;
	private final SymbolImportService symbolImportService;
	private final JwtTokenService jwtTokenProvider;

	public QuoteController(QuoteService quoteService, QuoteImportService quoteImportService,
			JwtTokenService jwtTokenProvider, PortfolioToIndexService portfolioToIndexService,
			CurrencyService currencyService, AppUserService appUserService, SymbolImportService symbolImportService) {
		this.quoteService = quoteService;
		this.quoteImportService = quoteImportService;
		this.portfolioToIndexService = portfolioToIndexService;
		this.currencyService = currencyService;
		this.appUserService = appUserService;
		this.symbolImportService = symbolImportService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@GetMapping("/daily/all/symbol/{symbol}")
	public List<QuoteDto> getAllDailyQuotes(@PathVariable("symbol") String symbol) {
		return this.quoteService.getDailyQuotes(symbol);
	}

	@GetMapping("/daily/all/portfolio/{portfolioId}/index/{indexSymbol}")
	public List<QuoteDto> getAllDailyComparisonIndexQuotes(@PathVariable("portfolioId") Long portfolioId,
			@PathVariable("indexSymbol") String indexSymbol) {
		ComparisonIndex comparisonIndex = List.of(ComparisonIndex.values()).stream()
				.filter(value -> value.getSymbol().equals(indexSymbol)).findFirst().orElseThrow();
		return this.portfolioToIndexService.calculateIndexComparison(portfolioId, comparisonIndex).stream()
				.map(DailyQuoteEntityDto::dto).toList();
	}

	@GetMapping("/intraday/symbol/{symbol}")
	public List<QuoteDto> getIntraDayQuotes(@PathVariable("symbol") String symbol) {
		return this.quoteService.getIntraDayQuotes(symbol);
	}

	@GetMapping("/daily/symbol/{symbol}/start/{start}/end/{end}")
	public List<QuoteDto> getDailyQuotesFromStartToEnd(@PathVariable("symbol") String symbol,
			@PathVariable("start") String isodateStart, @PathVariable("end") String isodateEnd) {
		LocalDate start = LocalDate.parse(isodateStart, DateTimeFormatter.ISO_DATE);
		LocalDate end = LocalDate.parse(isodateEnd, DateTimeFormatter.ISO_DATE);
		return this.quoteService.getDailyQuotes(symbol, start, end);
	}

	@GetMapping("/daily/portfolio/{portfolioId}/index/{indexSymbol}/start/{start}/end/{end}")
	public List<QuoteDto> getAllDailyComparisonIndexQuotesFromStartToEnd(@PathVariable("portfolioId") Long portfolioId,
			@PathVariable("indexSymbol") String indexSymbol, @PathVariable("start") String isodateStart,
			@PathVariable("end") String isodateEnd) {
		ComparisonIndex comparisonIndex = List.of(ComparisonIndex.values()).stream()
				.filter(value -> value.getSymbol().equals(indexSymbol)).findFirst().orElseThrow();
		LocalDate start = LocalDate.parse(isodateStart, DateTimeFormatter.ISO_DATE);
		LocalDate end = LocalDate.parse(isodateEnd, DateTimeFormatter.ISO_DATE);
		return this.portfolioToIndexService.calculateIndexComparison(portfolioId, comparisonIndex, start, end).stream()
				.map(DailyQuoteEntityDto::dto).toList();
	}

	@GetMapping("/import/daily/symbol/{symbol}")
	public Long importDailyQuotes(@PathVariable("symbol") String symbol, @RequestHeader("authorization") String token) {
		AppUserDto appUserDto = this.appUserService.loadByName(this.jwtTokenProvider.getUsername(this.jwtTokenProvider
				.resolveToken(token).orElseThrow(() -> new AuthenticationException("Invalid token."))), true);
		String alphavantageKey = this.symbolImportService.decrypt(appUserDto.getAlphavantageKey(),
				UUID.fromString(appUserDto.getUuid()));
		String rapidApiKey = this.symbolImportService.decrypt(appUserDto.getRapidApiKey(),
				UUID.fromString(appUserDto.getUuid()));
		return this.quoteImportService.importDailyQuoteHistory(symbol, new UserKeys(alphavantageKey, rapidApiKey));
	}

	@GetMapping("/import/intraday/symbol/{symbol}")
	public Long importIntraDayQuotes(@PathVariable("symbol") String symbol,
			@RequestHeader("authorization") String token) {
		AppUserDto appUserDto = this.appUserService.loadByName(this.jwtTokenProvider.getUsername(this.jwtTokenProvider
				.resolveToken(token).orElseThrow(() -> new AuthenticationException("Invalid token."))), true);		
		String alphavantageKey = this.symbolImportService.decrypt(appUserDto.getAlphavantageKey(),
				UUID.fromString(appUserDto.getUuid()));
		String rapidApiKey = this.symbolImportService.decrypt(appUserDto.getRapidApiKey(),
				UUID.fromString(appUserDto.getUuid()));
		return this.quoteImportService.importIntraDayQuotes(symbol, new UserKeys(alphavantageKey, rapidApiKey));
	}

	@GetMapping("/import/daily/currency/{to_curr}")
	public Long importFxDailyQuotes(@PathVariable("to_curr") String to_curr) {
		Long count = this.currencyService.importFxDailyQuoteHistory(to_curr);
		this.currencyService.initCurrencyMap();
		return count;
	}

	@GetMapping("/update/portfolio/symbols")
	public Long updatePortfolioSymbols() {
		LOGGER.info("updateSymbolQuotes started.");
		List<Symbol> symbolsToUpdate = this.symbolImportService.findSymbolsToUpdate();
		this.symbolImportService.updateSymbolQuotes(symbolsToUpdate);
		return 0L;
	}
	
	@PutMapping(path = "/importus/data")
	public String importFinancialData(@RequestBody ImportDataDto importFinancialDataDto) {
		this.quoteService.importUsDailyQuotes(importFinancialDataDto);		
		return "{\"status\": \"started\" }";
	}
}
