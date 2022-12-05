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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.dto.FilterNumberDto.Operation;
import ch.xxx.manager.domain.model.dto.FinancialElementParamDto;
import ch.xxx.manager.domain.model.dto.SfQuarterDto;
import ch.xxx.manager.domain.model.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.domain.model.entity.FinancialElement;
import ch.xxx.manager.domain.model.entity.SymbolFinancials;
import ch.xxx.manager.domain.model.entity.SymbolFinancialsRepository;
import ch.xxx.manager.domain.utils.DataHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

@Repository
public class SymbolFinancialsRepositoryBean implements SymbolFinancialsRepository {
	private final JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository;
	private final EntityManager entityManager;

	public SymbolFinancialsRepositoryBean(JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository,
			EntityManager entityManager) {
		this.jpaSymbolFinancialsRepository = jpaSymbolFinancialsRepository;
		this.entityManager = entityManager;
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
		return this.jpaSymbolFinancialsRepository.findCommonSfQuarters(Pageable.ofSize(20)).stream()
				.filter(myDto -> myDto.getTimesFound() >= 10).collect(Collectors.toList());
	}

	@Override
	public List<SymbolFinancials> findSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		CriteriaQuery<SymbolFinancials> createQuery = this.entityManager.getCriteriaBuilder()
				.createQuery(SymbolFinancials.class);
		Root<SymbolFinancials> root = createQuery.from(SymbolFinancials.class);
		List<Predicate> predicates = new ArrayList<>();
		if (symbolFinancialsQueryParams.getSymbol() != null || !symbolFinancialsQueryParams.getSymbol().isBlank()) {
			predicates.add(this.entityManager.getCriteriaBuilder().equal(
					this.entityManager.getCriteriaBuilder().lower(root.get("symbol")),
					symbolFinancialsQueryParams.getSymbol().trim().toLowerCase()));
		}
		if (symbolFinancialsQueryParams.getQuarters() != null && !symbolFinancialsQueryParams.getQuarters().isEmpty()) {
			predicates.add(this.entityManager.getCriteriaBuilder().in(root.get("quarter"))
					.value(symbolFinancialsQueryParams.getQuarters()));
		}
		if (symbolFinancialsQueryParams.getYearFilter() != null
				&& symbolFinancialsQueryParams.getYearFilter().getValue() != null
				&& symbolFinancialsQueryParams.getYearFilter().getOperation() != null) {
			if (Operation.SmallerEqual.equals(symbolFinancialsQueryParams.getYearFilter().getOperation())) {
				predicates.add(this.entityManager.getCriteriaBuilder().lessThanOrEqualTo(root.get("fiscalYear"),
						symbolFinancialsQueryParams.getYearFilter().getValue()));
			} else if (Operation.LargerEqual.equals(symbolFinancialsQueryParams.getYearFilter().getOperation())) {
				predicates.add(this.entityManager.getCriteriaBuilder().greaterThanOrEqualTo(root.get("fiscalYear"),
						symbolFinancialsQueryParams.getYearFilter().getValue()));
			} else if (Operation.Equal.equals(symbolFinancialsQueryParams.getYearFilter().getOperation())) {
				predicates.add(this.entityManager.getCriteriaBuilder().equal(root.get("fiscalYear"),
						symbolFinancialsQueryParams.getYearFilter().getValue()));
			}
		}
		if (symbolFinancialsQueryParams.getFinancialElementParams() != null
				&& !symbolFinancialsQueryParams.getFinancialElementParams().isEmpty()) {
			symbolFinancialsQueryParams.getFinancialElementParams().forEach(myDto -> {
				Metamodel m = this.entityManager.getMetamodel();
				EntityType<SymbolFinancials> symbolFinancials_ = m.entity(SymbolFinancials.class);
				financialElementConceptClause(root, predicates, myDto, symbolFinancials_);
				financialElementValueClause(root, predicates, myDto, symbolFinancials_);
			});
		}
		createQuery.where(predicates.toArray(new Predicate[0])).distinct(true);
		return this.entityManager.createQuery(createQuery).setMaxResults(200).getResultList();
	}

	private void financialElementValueClause(Root<SymbolFinancials> root, List<Predicate> predicates,
			FinancialElementParamDto myDto, EntityType<SymbolFinancials> symbolFinancials_) {
		if (myDto.getValueFilter() != null && myDto.getValueFilter().getOperation() != null
				&& myDto.getValueFilter().getValue() != null) {

			Path<BigDecimal> joinPath = root
					.join(symbolFinancials_.getDeclaredList("financialElements", FinancialElement.class)).get("value");
			if (myDto.getValueFilter().getOperation().equals(Operation.Equal)) {
				Predicate equalPredicate = this.entityManager.getCriteriaBuilder().equal(joinPath,
						myDto.getValueFilter().getValue());
				predicates.add(this.financialElementOperatorClause(myDto, equalPredicate));
			} else if (myDto.getValueFilter().getOperation().equals(Operation.SmallerEqual)) {
				Predicate lessThanOrEqualToPredicate = this.entityManager.getCriteriaBuilder()
						.lessThanOrEqualTo(joinPath, myDto.getValueFilter().getValue());
				predicates.add(this.financialElementOperatorClause(myDto, lessThanOrEqualToPredicate));
			} else if (myDto.getValueFilter().getOperation().equals(Operation.LargerEqual)) {
				Predicate greaterThanOrEqualToPredicate = this.entityManager.getCriteriaBuilder()
						.greaterThanOrEqualTo(joinPath, myDto.getValueFilter().getValue());
				predicates.add(this.financialElementOperatorClause(myDto, greaterThanOrEqualToPredicate));
			}
		}
	}

	private void financialElementConceptClause(Root<SymbolFinancials> root, List<Predicate> predicates,
			FinancialElementParamDto myDto, EntityType<SymbolFinancials> symbolFinancials_) {
		if (myDto.getConceptFilter().getOperation() != null && myDto.getConceptFilter().getValue() != null
				&& myDto.getConceptFilter().getValue().trim().length() > 2) {
			Expression<String> lowerExp = this.entityManager.getCriteriaBuilder()
					.lower(root.join(symbolFinancials_.getDeclaredList("financialElements", FinancialElement.class))
							.get("concept"));
			if (!myDto.getConceptFilter().getOperation()
					.equals(ch.xxx.manager.domain.model.dto.FilterStringDto.Operation.Equal)) {
				String filterStr = String.format("%%%s%%", myDto.getConceptFilter().getValue().trim().toLowerCase());
				if (myDto.getConceptFilter().getOperation()
						.equals(ch.xxx.manager.domain.model.dto.FilterStringDto.Operation.StartsWith)) {
					String.format("%s%%", myDto.getConceptFilter().getValue().trim().toLowerCase());
				} else if (myDto.getConceptFilter().getOperation()
						.equals(ch.xxx.manager.domain.model.dto.FilterStringDto.Operation.EndsWith)) {
					String.format("%%%s", myDto.getConceptFilter().getValue().trim().toLowerCase());
				}
				Predicate likePredicate = this.entityManager.getCriteriaBuilder().like(lowerExp, filterStr);
				predicates.add(financialElementOperatorClause(myDto, likePredicate));
			} else {
				Predicate equalPredicate = this.entityManager.getCriteriaBuilder().equal(lowerExp,
						myDto.getConceptFilter().getValue().trim().toLowerCase());
				predicates.add(this.financialElementOperatorClause(myDto, equalPredicate));
			}
		}
	}

	private Predicate financialElementOperatorClause(FinancialElementParamDto myDto, Predicate likePredicate) {
		Predicate resultPredicate = null;
		if (myDto.getOperation().equals(DataHelper.Operation.And)) {
			resultPredicate = this.entityManager.getCriteriaBuilder().and(likePredicate);
		} else if (myDto.getOperation().equals(DataHelper.Operation.AndNot)) {
			resultPredicate = this.entityManager.getCriteriaBuilder()
					.and(this.entityManager.getCriteriaBuilder().not(likePredicate));
		} else if (myDto.getOperation().equals(DataHelper.Operation.Or)) {
			resultPredicate = this.entityManager.getCriteriaBuilder().or(likePredicate);
		} else if (myDto.getOperation().equals(DataHelper.Operation.OrNot)) {
			resultPredicate = this.entityManager.getCriteriaBuilder()
					.or(this.entityManager.getCriteriaBuilder().not(likePredicate));
		}
		return resultPredicate;
	}
}
