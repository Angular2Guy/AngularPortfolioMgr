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
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.domain.model.dto.FeConceptDto;
import ch.xxx.manager.domain.model.dto.FeIdInfoDto;
import ch.xxx.manager.domain.model.dto.ImportFinancialDataDto;
import ch.xxx.manager.domain.model.dto.SfCountryDto;
import ch.xxx.manager.domain.model.dto.SfQuarterDto;
import ch.xxx.manager.domain.model.dto.SymbolFinancialsDto;
import ch.xxx.manager.domain.model.dto.SymbolFinancialsIdParamDto;
import ch.xxx.manager.domain.model.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.domain.model.dto.SymbolNameDto;
import ch.xxx.manager.usecase.mapping.SymbolFinancialsMapper;
import ch.xxx.manager.usecase.service.FinancialDataService;
import ch.xxx.manager.usecase.service.SymbolService;

@RestController
@RequestMapping("rest/financialdata")
public class FinancialDataController {
	private static final Logger LOGGER = LoggerFactory.getLogger(FinancialDataController.class);
	private final SymbolService symbolService;
	private final FinancialDataService financialDataService;
	private final SymbolFinancialsMapper symbolFinancialsMapper;

	public FinancialDataController(SymbolService symbolService, FinancialDataService financialDataService,
			SymbolFinancialsMapper symbolFinancialsMapper) {
		this.symbolService = symbolService;
		this.financialDataService = financialDataService;
		this.symbolFinancialsMapper = symbolFinancialsMapper;
	}

	@GetMapping("/financialelement/concept/all")
	public List<FeConceptDto> getFeConcepts() {
		return this.financialDataService.findFeConcepts();
	}

	@GetMapping("/symbolfinancials/quarters/all")
	public List<SfQuarterDto> getSfQuarters() {
		return this.financialDataService.findSfQuarters();
	}

	@GetMapping("/symbolfinancials/countries/all")
	public List<SfCountryDto> getSfCountries() {
		return this.financialDataService.findSfCountries();
	}

	@GetMapping("/financialelement/concept/{concept}")
	public List<FeConceptDto> getFeConcepts(@PathVariable("concept") String concept) {
		return this.financialDataService.findFeConcepts().stream().filter(myDto -> Optional
				.ofNullable(myDto.getConcept()).stream().anyMatch(myConcept -> myConcept.contains(concept))).toList();
	}

	@GetMapping("/financialelement/id/{id}")
	public FeIdInfoDto getFeInfo(@PathVariable("id") Long id) {
		return this.financialDataService.findFeInfo(id);
	}
	
	@GetMapping("/symbolfinancials/companyname/{companyname}")
	public List<SymbolNameDto> findSymbolNameByName(@PathVariable("companyname") String companyName) {
		return this.financialDataService.findSymbolFinancialsByName(companyName).stream()
				.map(mySymbolFinancials -> this.symbolFinancialsMapper.toRc(mySymbolFinancials)).toList();
	}

	@GetMapping("/symbolfinancials/symbol/{symbol}")
	public List<SymbolNameDto> findSymbolNameBySymbol(@PathVariable("symbol") String symbol) {
		return this.financialDataService.findSymbolFinancialsBySymbol(symbol).stream()
				.map(mySymbolFinancials -> this.symbolFinancialsMapper.toRc(mySymbolFinancials)).toList();
	}		
	
	@PostMapping("/search/params")
	public List<SymbolFinancialsDto> findSymbolFinancials(
			@RequestBody SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		return this.financialDataService.findSymbolFinancials(symbolFinancialsQueryParams).stream()
				.map(mySymbolFe -> this.symbolFinancialsMapper.toDto(mySymbolFe)).collect(Collectors.toList());
	}

	@PostMapping("/search/symbolfinancials/ids")
	public List<SymbolFinancialsDto> findSymbolFinancialsByIds(
			@RequestBody SymbolFinancialsIdParamDto symbolFinancialsIdParamDto) {
		return this.financialDataService.findSymbolFinancialsByIds(symbolFinancialsIdParamDto.getSymbolFinancialsIds())
				.stream().map(mySymbolFinancials -> this.symbolFinancialsMapper.toDto(mySymbolFinancials))
				.collect(Collectors.toList());
	}

	@PutMapping(path = "/importus/data")
	public String importFinancialData(@RequestBody ImportFinancialDataDto importFinancialDataDto) {
		this.symbolService.importFinancialData(importFinancialDataDto);
		return "{\"status\": \"started\" }";
	}
}
