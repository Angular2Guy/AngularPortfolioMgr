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

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ch.xxx.manager.findata.dto.FeConceptDto;
import ch.xxx.manager.findata.dto.FeIdInfoDto;
import ch.xxx.manager.findata.entity.FinancialElement;

public interface JpaFinancialElementRepository extends JpaRepository<FinancialElement, Long> {
	@Modifying
	@Query(nativeQuery = true, value = "drop index if exists ix_financial_element_symbol_financials_id")
	void dropSymbolFinancialsIdIndex();
	@Modifying
	@Query(nativeQuery = true, value = "drop index if exists ix_financial_element_concept")
	void dropConceptIndex();
	@Modifying
	@Query(nativeQuery = true, value = "alter table financial_element drop constraint if exists fk_financial_element_symbol_financials_id")
	void dropFkConstraintSymbolFinancials();
	@Modifying
	@Query(nativeQuery = true, value = "alter table financial_element add constraint fk_financial_element_symbol_financials_id foreign key (symbol_financials_id) references symbol_financials(id)")
	void createFkConstraintSymbolFinancials();
	@Modifying
	@Query(nativeQuery = true, value = "create index ix_financial_element_symbol_financials_id on financial_element (symbol_financials_id)")
	void createSymbolFinancialsIdIndex();
	@Modifying
	@Query(nativeQuery = true, value = "create index ix_financial_element_concept on financial_element (concept)")
	void createConceptIndex();
	@Modifying
	@Query(nativeQuery = true, value = "create index ix_financial_element_financial_element_type on financial_element (financial_element_type)")
	void createFinancialElementTypeIndex();
	@Modifying
	@Query(nativeQuery = true, value = "drop index if exists ix_financial_element_financial_element_type")
	void dropFinancialElementTypeIndex();
	@Query(value = "select new ch.xxx.manager.domain.model.dto.FeConceptDto(fe.concept, count(fe.id) as concept_count) from FinancialElement fe group by fe.concept order by concept_count desc")	
	List<FeConceptDto> findCommonFeConcepts(Pageable pageable);
	@Query(value = "select new ch.xxx.manager.domain.model.dto.FeIdInfoDto(fe.id, fe.info) from FinancialElement fe where fe.id = :id")
	FeIdInfoDto findFeIdInfoById(@Param("id") Long id);
}
