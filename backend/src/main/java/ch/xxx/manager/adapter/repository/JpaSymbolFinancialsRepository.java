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

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ch.xxx.manager.domain.model.dto.SfCountryDto;
import ch.xxx.manager.domain.model.dto.SfQuarterDto;
import ch.xxx.manager.domain.model.entity.SymbolFinancials;

public interface JpaSymbolFinancialsRepository extends JpaRepository<SymbolFinancials, Long> {	
	@Query(value = "select new ch.xxx.manager.domain.model.dto.SfQuarterDto(sf.quarter, count(sf.id) as quarter_count) from SymbolFinancials sf group by sf.quarter order by quarter_count desc")	
	List<SfQuarterDto> findCommonSfQuarters(Pageable pageable);
	@Query(value = "select new ch.xxx.manager.domain.model.dto.SfCountryDto(upper(sf.country), count(sf.id) as quarter_count) from SymbolFinancials sf group by sf.country order by quarter_count desc")	
	List<SfCountryDto> findCommonSfCountries(Pageable pageable);
	@Query(value = "select sf from SymbolFinancials sf join fetch sf.financialElements fe where sf.id in (:ids)")
	List<SymbolFinancials> findAllByIdFetchEager(@Param(value = "ids") Collection<Long> ids);
	List<SymbolFinancials> findBySymbol(String symbol);
}
