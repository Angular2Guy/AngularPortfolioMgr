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
package ch.xxx.manager.findata.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.xxx.manager.stocks.dto.FeConceptDto;
import ch.xxx.manager.stocks.dto.FeIdInfoDto;
import ch.xxx.manager.stocks.dto.SfCountryDto;
import ch.xxx.manager.stocks.dto.SfQuarterDto;
import ch.xxx.manager.stocks.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.findata.entity.FinancialElementRepository;
import ch.xxx.manager.stocks.entity.SymbolFinancials;
import ch.xxx.manager.stocks.entity.SymbolFinancialsRepository;
import ch.xxx.manager.stocks.entity.dto.SymbolFinancialsDto;
import ch.xxx.manager.common.utils.StreamHelpers;
import ch.xxx.manager.stocks.mapping.SymbolFinancialsImportMapper;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Service
public class FinancialDataService {
	private static final Logger LOGGER = LoggerFactory.getLogger(FinancialDataService.class);
	private final SymbolFinancialsImportMapper symbolFinancialsMapper;
	private final SymbolFinancialsRepository symbolFinancialsRepository;
	private final FinancialElementRepository financialElementRepository;
	private final List<FeConceptDto> feConcepts = new CopyOnWriteArrayList<>();
	private final List<SfQuarterDto> sfQuarters = new CopyOnWriteArrayList<>();
	private final List<SfCountryDto> sfCountries = new CopyOnWriteArrayList<>();

	public FinancialDataService(SymbolFinancialsImportMapper symbolFinancialsMapper,
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

	@Transactional(value = TxType.REQUIRES_NEW)
	public void dropFeIndexes() {
		this.financialElementRepository.dropFkConstraintSymbolFinancials();
		this.financialElementRepository.dropConceptIndex();
		this.financialElementRepository.dropSymbolFinancialsIdIndex();
		this.financialElementRepository.dropFinancialElementTypeIndex();
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public void createFeIndexes() {
		this.financialElementRepository.createFkConstraintSymbolFinancials();
		this.financialElementRepository.createConceptIndex();
		this.financialElementRepository.createSymbolFinancialsIdIndex();
		this.financialElementRepository.createFinancialElementTypeIndex();
	}

	@Transactional
	public List<SymbolFinancials> findSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		return this.symbolFinancialsRepository.findSymbolFinancials(symbolFinancialsQueryParams);
	}

	@Transactional
	public List<SymbolFinancials> findSymbolFinancialsByIds(List<Long> symbolsFinancialsIds) {
		return symbolsFinancialsIds.size() > 250 ? List.of()
				: this.symbolFinancialsRepository.findAllByIdFetchEager(symbolsFinancialsIds);
	}

	@Transactional
	public List<FeConceptDto> findFeConcepts() {
		if (this.feConcepts.isEmpty()) {
			this.updateFeConcepts();
		}
		return this.feConcepts;
	}

	@Transactional
	public void updateSfQuarters() {
		this.sfQuarters.clear();
		this.sfQuarters.addAll(this.symbolFinancialsRepository.findCommonSfQuarters());
	}

	@Transactional
	public List<SfQuarterDto> findSfQuarters() {
		if (this.sfQuarters.isEmpty()) {
			this.updateSfQuarters();
		}
		return this.sfQuarters;
	}

	@Transactional
	public void updateSfCountries() {
		this.sfCountries.clear();
		this.sfCountries.addAll(this.symbolFinancialsRepository.findCommonSfCountries());
	}

	@Transactional
	public List<SfCountryDto> findSfCountries() {
		if (this.sfCountries.isEmpty()) {
			this.updateSfCountries();
		}
		return this.sfCountries;
	}

	@Transactional
	public void updateFeConcepts() {
		this.feConcepts.clear();
		this.feConcepts.addAll(this.financialElementRepository.findCommonFeConcepts());
	}

	@Transactional
	public Collection<SymbolFinancials> findSymbolFinancialsByName(String companyName) {
		return companyName == null || companyName.trim().isBlank() ? List.of()
				: this.symbolFinancialsRepository.findByName(companyName).stream()
						.filter(StreamHelpers.distinctByKey(SymbolFinancials::getName)).toList();
	}

	@Transactional
	public FeIdInfoDto findFeInfo(Long id) {
		return this.financialElementRepository.findFeIdInfoById(id);
	}
	
	@Transactional
	public Collection<SymbolFinancials> findSymbolFinancialsBySymbol(String symbol) {
		return symbol == null || symbol.trim().isBlank() ? List.of()
				: this.symbolFinancialsRepository.findBySymbol(symbol).stream()
				.filter(StreamHelpers.distinctByKey(SymbolFinancials::getSymbol)).toList();
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public void storeFinancialsData(List<SymbolFinancialsDto> symbolFinancialsDtos) {
		Set<SymbolFinancials> symbolFinancials = symbolFinancialsDtos.stream()
				.map(this.symbolFinancialsMapper::toEntity).collect(Collectors.toSet());
		this.symbolFinancialsRepository.saveAll(symbolFinancials);
		this.financialElementRepository.saveAll(symbolFinancials.stream()
				.flatMap(mySymbolFinancials -> mySymbolFinancials.getFinancialElements().stream())
				.collect(Collectors.toSet()));
		LOGGER.info("Items imported: {}", symbolFinancials.size());
	}
}
