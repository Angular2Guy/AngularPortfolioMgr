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
package ch.xxx.manager.adapter.repository;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import ch.xxx.manager.domain.model.dto.SfCountryDto;
import ch.xxx.manager.domain.model.dto.SfQuarterDto;
import ch.xxx.manager.domain.model.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.domain.model.entity.SymbolFinancials;
import ch.xxx.manager.domain.model.entity.SymbolFinancialsRepository;


public abstract class SymbolFinancialsRepositoryBaseBean implements SymbolFinancialsRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolFinancialsRepositoryBaseBean.class);
	protected final JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository;

	public SymbolFinancialsRepositoryBaseBean(JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository) {
		this.jpaSymbolFinancialsRepository = jpaSymbolFinancialsRepository;
	}

	@Override
	public SymbolFinancials save(SymbolFinancials symbolfinancials) {
		return this.jpaSymbolFinancialsRepository.save(symbolfinancials);
	}

	@Override
	public List<SymbolFinancials> saveAll(Iterable<SymbolFinancials> symbolfinancials) {
		return this.jpaSymbolFinancialsRepository.saveAll(symbolfinancials);
	}

	@Override
	public Optional<SymbolFinancials> findById(Long id) {
		return this.jpaSymbolFinancialsRepository.findById(id);
	}

	@Override
	public void deleteAllBatch() {
		this.jpaSymbolFinancialsRepository.deleteAllInBatch();
	}

	@Override
	public List<SfQuarterDto> findCommonSfQuarters() {
		return this.jpaSymbolFinancialsRepository.findCommonSfQuarters(Pageable.ofSize(20)).stream()
				.filter(myDto -> myDto.getTimesFound() >= 10).collect(Collectors.toList());
	}
	
	@Override
	public List<SfCountryDto> findCommonSfCountries() {
		return this.jpaSymbolFinancialsRepository.findCommonSfCountries(Pageable.unpaged());
	}
	
	@Override
	public abstract List<SymbolFinancials> findSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams);

	@Override
	public List<SymbolFinancials> findByName(String companyName) {	
		return this.jpaSymbolFinancialsRepository.findByName(companyName.trim().toLowerCase(), Pageable.ofSize(20));
	}

	@Override
	public List<SymbolFinancials> findBySymbol(String symbol) {	
		return this.jpaSymbolFinancialsRepository.findBySymbol(symbol.trim().toLowerCase(), Pageable.ofSize(20));
	}
	
	@Override
	public List<SymbolFinancials> findAllByIdFetchEager(Collection<Long> ids) {
		LocalTime start = LocalTime.now();
		List<SymbolFinancials> results = this.jpaSymbolFinancialsRepository.findAllByIdFetchEager(ids);
		LOGGER.info("Query1 ids: {}, time: {} ms", ids.size(), Duration.between(start, LocalTime.now()).toMillis());
		return results;
	}
}
