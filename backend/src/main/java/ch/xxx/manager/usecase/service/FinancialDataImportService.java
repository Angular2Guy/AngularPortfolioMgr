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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.xxx.manager.domain.model.dto.FeConceptDto;
import ch.xxx.manager.domain.model.entity.FinancialElement;
import ch.xxx.manager.domain.model.entity.FinancialElementRepository;
import ch.xxx.manager.domain.model.entity.SymbolFinancials;
import ch.xxx.manager.domain.model.entity.SymbolFinancialsRepository;
import ch.xxx.manager.domain.model.entity.dto.SymbolFinancialsDto;
import ch.xxx.manager.usecase.mapping.SymbolFinancialsMapper;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Service
public class FinancialDataImportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(FinancialDataImportService.class);
	private final SymbolFinancialsMapper symbolFinancialsMapper;
	private final SymbolFinancialsRepository symbolFinancialsRepository;
	private final FinancialElementRepository financialElementRepository;
	private final List<FeConceptDto> feConcepts = new CopyOnWriteArrayList<>();

	public FinancialDataImportService(SymbolFinancialsMapper symbolFinancialsMapper,
			SymbolFinancialsRepository symbolFinancialsRepository,
			FinancialElementRepository financialElementRepository) {
		this.symbolFinancialsMapper = symbolFinancialsMapper;
		this.symbolFinancialsRepository = symbolFinancialsRepository;
		this.financialElementRepository = financialElementRepository;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public void clearFinancialsData() {
		this.financialElementRepository.deleteAllBatch();
		this.symbolFinancialsRepository.deleteAllBatch();		
	}
	
	@Transactional(value=TxType.REQUIRES_NEW)
	public void dropFeIndexes() {
		this.financialElementRepository.dropConceptIndex();
		this.financialElementRepository.dropSymbolFinancialsIdIndex();
	}
	
	@Transactional(value=TxType.REQUIRES_NEW)
	public void createFeIndexes() {
		this.financialElementRepository.createConceptIndex();
		this.financialElementRepository.createSymbolFinancialsIdIndex();
	}
	
	@Transactional
	public List<FeConceptDto> findFeConcepts() {
		if(this.feConcepts.isEmpty()) {
			this.updateFeConcepts();
		}
		return this.feConcepts;
	}
	
	@Transactional
	public void updateFeConcepts() {
		this.feConcepts.clear();
		this.feConcepts.addAll(this.financialElementRepository.findCommonFeConcepts());		
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void storeFinancialsData(List<SymbolFinancialsDto> symbolFinancialsDtos) {
		Set<SymbolFinancials> symbolFinancials = symbolFinancialsDtos.stream().map(myDto -> this.symbolFinancialsMapper.toEntity(myDto)).collect(Collectors.toSet());
		this.symbolFinancialsRepository.saveAll(symbolFinancials);
		this.financialElementRepository.saveAll(symbolFinancials.stream()
				.map(mySymbolFinancials -> concatFinancialElemenst(mySymbolFinancials)).flatMap(Set::stream).collect(Collectors.toSet()));
		LOGGER.info("Items imported: {}", symbolFinancials.size());
	}

	private Set<FinancialElement> concatFinancialElemenst(SymbolFinancials mySymbolFinancials) {
		Set<FinancialElement> financialElements = new HashSet<>();
		financialElements.addAll(mySymbolFinancials.getBalanceSheet());
		financialElements.addAll(mySymbolFinancials.getCashFlow());
		financialElements.addAll(mySymbolFinancials.getIncome());
		return financialElements;
	}
}
