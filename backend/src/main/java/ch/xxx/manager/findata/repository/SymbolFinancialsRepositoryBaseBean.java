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
package ch.xxx.manager.findata.repository;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import ch.xxx.manager.findata.dto.SfCountryDto;
import ch.xxx.manager.findata.dto.SfQuarterDto;
import ch.xxx.manager.findata.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.findata.entity.SymbolFinancials;


public abstract class SymbolFinancialsRepositoryBaseBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolFinancialsRepositoryBaseBean.class);
	protected final JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository;

	public SymbolFinancialsRepositoryBaseBean(JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository) {
		this.jpaSymbolFinancialsRepository = jpaSymbolFinancialsRepository;
	}

	public SymbolFinancials save(SymbolFinancials symbolfinancials) {
		return this.jpaSymbolFinancialsRepository.save(symbolfinancials);
	}

	public List<SymbolFinancials> saveAll(Iterable<SymbolFinancials> symbolfinancials) {
		return this.jpaSymbolFinancialsRepository.saveAll(symbolfinancials);
	}

	public Optional<SymbolFinancials> findById(Long id) {
		return this.jpaSymbolFinancialsRepository.findById(id);
	}

	public void deleteAllBatch() {
		this.jpaSymbolFinancialsRepository.deleteAllInBatch();
	}

	public List<SfQuarterDto> findCommonSfQuarters() {
		return this.jpaSymbolFinancialsRepository.findCommonSfQuarters(Pageable.ofSize(20)).stream()
				.filter(myDto -> myDto.getTimesFound() >= 10).collect(Collectors.toList());
	}
	
	public List<SfCountryDto> findCommonSfCountries() {
		return this.jpaSymbolFinancialsRepository.findCommonSfCountries(Pageable.unpaged());
	}
	
	public abstract List<SymbolFinancials> findSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams);

	public List<SymbolFinancials> findByName(String companyName) {
		return this.jpaSymbolFinancialsRepository.findByName(companyName.trim().toLowerCase(), Pageable.ofSize(20));
	}

	public List<SymbolFinancials> findBySymbol(String symbol) {
		return this.jpaSymbolFinancialsRepository.findBySymbol(symbol.trim().toLowerCase(), Pageable.ofSize(20));
	}
	
	public List<SymbolFinancials> findAllByIdFetchEager(Collection<Long> ids) {
		LocalTime start = LocalTime.now();
		List<SymbolFinancials> results = this.jpaSymbolFinancialsRepository.findAllByIdFetchEager(ids);
		LOGGER.info("Query1 ids: {}, time: {} ms", ids.size(), Duration.between(start, LocalTime.now()).toMillis());
		return results;
	}
}
