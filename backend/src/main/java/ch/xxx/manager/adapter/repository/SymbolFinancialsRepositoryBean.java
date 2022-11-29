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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.dto.SfQuarterDto;
import ch.xxx.manager.domain.model.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.domain.model.entity.SymbolFinancials;
import ch.xxx.manager.domain.model.entity.SymbolFinancialsRepository;

@Repository
public class SymbolFinancialsRepositoryBean implements SymbolFinancialsRepository {
	private final JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository;

	public SymbolFinancialsRepositoryBean(JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository) {
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

	public List<SfQuarterDto> findCommonSfQuarters() {
		return this.jpaSymbolFinancialsRepository.findCommonSfQuarters(Pageable.ofSize(20))
				.stream().filter(myDto -> myDto.getTimesFound() >= 10)
				.collect(Collectors.toList());
	}

	@Override
	public List<SymbolFinancials> findSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		return List.of();
	}
}
