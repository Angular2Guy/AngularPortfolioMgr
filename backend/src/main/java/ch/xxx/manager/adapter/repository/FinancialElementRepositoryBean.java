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

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.FinancialElement;
import ch.xxx.manager.domain.model.entity.FinancialElementRepository;

@Repository
public class FinancialElementRepositoryBean implements FinancialElementRepository {
	private final JpaFinancialElementRepository jpaFinancialElementRepository;

	public FinancialElementRepositoryBean(JpaFinancialElementRepository jpaFinancialElementRepository) {
		this.jpaFinancialElementRepository = jpaFinancialElementRepository;
	}	

	@Override
	public FinancialElement save(FinancialElement financialElement) {
		return this.jpaFinancialElementRepository.save(financialElement);
	}
	
	@Override
	public List<FinancialElement> saveAll(Iterable<FinancialElement> symbols) {
		return this.jpaFinancialElementRepository.saveAll(symbols);
	}

	@Override
	public Optional<FinancialElement> findById(Long id) {
		return this.jpaFinancialElementRepository.findById(id);
	}

	@Override
	public void deleteAll() {
		this.jpaFinancialElementRepository.deleteAll();
	}
}
