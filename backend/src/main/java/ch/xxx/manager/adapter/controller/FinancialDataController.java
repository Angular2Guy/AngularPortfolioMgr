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
import ch.xxx.manager.domain.model.dto.FinancialElementDto;
import ch.xxx.manager.domain.model.dto.ImportFinancialDataDto;
import ch.xxx.manager.domain.model.dto.SfQuarterDto;
import ch.xxx.manager.domain.model.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.usecase.service.FinancialDataService;
import ch.xxx.manager.usecase.service.SymbolService;

@RestController
@RequestMapping("rest/financialdata")
public class FinancialDataController {
	private static final Logger LOGGER = LoggerFactory.getLogger(FinancialDataController.class);
	private final SymbolService symbolService;
	private final FinancialDataService financialDataService;

	public FinancialDataController(SymbolService symbolService, FinancialDataService financialDataService) {
		this.symbolService = symbolService;
		this.financialDataService = financialDataService;
	}

	@GetMapping("/financialelement/concept/all")
	public List<FeConceptDto> getFeConcepts() {
		return this.financialDataService.findFeConcepts();
	}

	@GetMapping("/symbolfinancials/quarters/all")
	public List<SfQuarterDto> getSfQuarters() {
		return this.financialDataService.findSfQuarters();
	}
	
	@GetMapping("/financialelement/concept/{concept}")
	public List<FeConceptDto> getFeConcepts(@PathVariable("concept") String concept) {
		return this.financialDataService.findFeConcepts().stream().filter(
				myDto -> Optional.ofNullable(myDto.getConcept()).stream().anyMatch(myConcept -> myConcept.contains(concept))).toList();
	}

	@PostMapping("/search/params")
	public List<FinancialElementDto> findSymbolFinancials(@RequestBody SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		return List.of(new FinancialElementDto());
	}
	
	@PutMapping(path = "/importus/data")
	public String importFinancialData(@RequestBody ImportFinancialDataDto importFinancialDataDto) {
		this.symbolService.importFinancialData(importFinancialDataDto);
		return "{\"status\": \"started\" }";
	}
}
