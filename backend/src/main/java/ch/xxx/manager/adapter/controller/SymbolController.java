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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.domain.model.dto.SymbolDto;
import ch.xxx.manager.usecase.service.ComparisonIndex;
import ch.xxx.manager.usecase.service.QuoteImportService;
import ch.xxx.manager.usecase.service.QuoteImportService.UserKeys;
import ch.xxx.manager.usecase.service.SymbolImportService;
import ch.xxx.manager.usecase.service.SymbolService;

@RestController
@RequestMapping("rest/symbol")
public class SymbolController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolController.class);
	private final SymbolImportService importService;
	private final SymbolService symbolService;
	private final QuoteImportService quoteImportService;
	@Value("${api.key}")
	private String apiKey;
	@Value("${api.key.rapidapi}")
	private String rapidApiKey;

	public SymbolController(SymbolImportService importService, SymbolService service,
			QuoteImportService quoteImportService) {
		this.importService = importService;
		this.symbolService = service;
		this.quoteImportService = quoteImportService;
	}

	@GetMapping(path = "/importus/all", produces = MediaType.TEXT_PLAIN_VALUE)
	public String importUsSymbols() {
		String result = this.importService.importUsSymbols();
		return result;
	}

	@GetMapping(path = "/importhk/all", produces = MediaType.TEXT_PLAIN_VALUE)
	public String importHkSymbols() {
		String result = this.importService.importHkSymbols();
		return result;
	}

	@GetMapping(path = "/importde/all", produces = MediaType.TEXT_PLAIN_VALUE)
	public String importDeSymbols() {
		String result = this.importService.importDeSymbols();
		return result;
	}

	@GetMapping(path = "/importindex/all", produces = MediaType.TEXT_PLAIN_VALUE)
	public String importIndexSymbols() {
		List<String> symbols = this.importService.importReferenceIndexes(List.of(ComparisonIndex.SP500.getSymbol(),
				ComparisonIndex.EUROSTOXX50.getSymbol(), ComparisonIndex.MSCI_CHINA.getSymbol()));
		Long symbolCount = symbols.stream().map(mySymbol -> this.quoteImportService.importUpdateDailyQuotes(mySymbol,
				new UserKeys(this.apiKey, this.rapidApiKey))).reduce(0L, (acc, value) -> acc + value);
		LOGGER.info("Indexquotes import done for: {}", symbolCount);
		return String.format("Indexquotes import done for: %d", symbolCount);
	}

	@GetMapping("/all")
	public List<SymbolDto> getAllSymbols() {
		return this.symbolService.getAllSymbols();
	}

	@GetMapping("/symbol/{symbol}")
	public List<SymbolDto> getSymbolBySymbol(@PathVariable("symbol") String symbol) {
		return this.symbolService.getSymbolBySymbol(symbol);
	}

	@GetMapping("/name/{name}")
	public List<SymbolDto> getSymbolByName(@PathVariable("name") String name) {
		return this.symbolService.getSymbolByName(name);
	}
}
