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
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.dto.FilterNumberDto.Operation;
import ch.xxx.manager.domain.model.dto.FilterStringDto;
import ch.xxx.manager.domain.model.dto.FinancialElementParamDto;
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
public class SymbolFinancialsRepositoryBean extends SymbolFinancialsRepositoryBaseBean implements SymbolFinancialsRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolFinancialsRepositoryBean.class);
	private final EntityManager entityManager;

	private record TermCollection(Collection<Predicate> and, Collection<Predicate> andNot, Collection<Predicate> or,
			Collection<Predicate> orNot) {
	}

	public SymbolFinancialsRepositoryBean(JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository,
			JpaFinancialElementRepository jpaFinancialElementRepository, EntityManager entityManager) {
		super(jpaSymbolFinancialsRepository);
		this.entityManager = entityManager;
	}
	
	@Override
	public List<SymbolFinancials> findSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		List<SymbolFinancials> result = List.of();
		record SfAndFe(SymbolFinancials symbolFinancials, List<FinancialElement> financialElements) {
		}
		if (symbolFinancialsQueryParams.getFinancialElementParams() != null
				&& !symbolFinancialsQueryParams.getFinancialElementParams().isEmpty()
				&& (symbolFinancialsQueryParams.getSymbol() == null
						|| symbolFinancialsQueryParams.getSymbol().isBlank())
				&& (symbolFinancialsQueryParams.getQuarters() == null
						|| symbolFinancialsQueryParams.getQuarters().isEmpty())
				&& (symbolFinancialsQueryParams.getYearFilter() == null
						|| symbolFinancialsQueryParams.getYearFilter().getValue() == null
						|| 0 < BigDecimal.valueOf(1800)
								.compareTo(symbolFinancialsQueryParams.getYearFilter().getValue())
						|| symbolFinancialsQueryParams.getYearFilter().getOperation() == null)) {
			LocalTime start1 = LocalTime.now();
			Set<FinancialElement> financialElements = this
					.findFinancialElements(symbolFinancialsQueryParams.getFinancialElementParams());
			final Map<Long, SfAndFe> sfToFeMap = new HashMap<>();
			financialElements.forEach(myFe -> {
				this.entityManager.detach(myFe);
				this.entityManager.detach(myFe.getSymbolFinancials());
				if (!sfToFeMap.containsKey(myFe.getSymbolFinancials().getId())) {
					sfToFeMap.put(myFe.getSymbolFinancials().getId(),
							new SfAndFe(myFe.getSymbolFinancials(), new ArrayList<>(List.of(myFe))));
				}
				sfToFeMap.get(myFe.getSymbolFinancials().getId()).financialElements().add(myFe);
			});
			result = sfToFeMap.entrySet().stream().map(myEntry -> {
				myEntry.getValue().symbolFinancials()
						.setFinancialElements(new HashSet<>(myEntry.getValue().financialElements()));
				return myEntry.getValue().symbolFinancials();
			}).collect(Collectors.toList());
			LOGGER.info("Query1: {} ms", Duration.between(start1, LocalTime.now()).toMillis());
			return result;
		}

		final CriteriaQuery<SymbolFinancials> createQuery = this.entityManager.getCriteriaBuilder()
				.createQuery(SymbolFinancials.class);
		final Root<SymbolFinancials> root = createQuery.from(SymbolFinancials.class);

		final List<Predicate> predicates = createSymbolFinancials(symbolFinancialsQueryParams, root);
		Metamodel m = this.entityManager.getMetamodel();
		EntityType<SymbolFinancials> symbolFinancials_ = m.entity(SymbolFinancials.class);
		root.fetch("financialElements");
		Path<FinancialElement> fePath = root.get("financialElements");
		this.createFinancialElementClauses(symbolFinancialsQueryParams.getFinancialElementParams(), fePath, predicates,
				Optional.of(symbolFinancials_));
		if (!predicates.isEmpty()) {
			createQuery.where(predicates.toArray(new Predicate[0])).distinct(true);
		} else {
			return new LinkedList<>();
		}
		LocalTime start1 = LocalTime.now();
		final List<SymbolFinancials> myResult = this.entityManager.createQuery(createQuery).getResultStream().limit(200)
				.collect(Collectors.toList());
		LOGGER.info("Query1: {} ms", Duration.between(start1, LocalTime.now()).toMillis());
		result = myResult;
//		LocalTime start2 = LocalTime.now();
//		result = this.jpaSymbolFinancialsRepository
//				.findAllByIdFetchEager(myResult.stream().map(SymbolFinancials::getId).collect(Collectors.toList()));
//		LOGGER.info("Query2: {} ms", Duration.between(start2, LocalTime.now()).toMillis());
		return result;
	}

	private List<Predicate> createSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams,
			final Root<SymbolFinancials> root) {
		final List<Predicate> predicates = new ArrayList<>();
		if (symbolFinancialsQueryParams.getSymbol() != null && !symbolFinancialsQueryParams.getSymbol().isBlank()) {
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
				&& 0 >= BigDecimal.valueOf(1800).compareTo(symbolFinancialsQueryParams.getYearFilter().getValue())
				&& symbolFinancialsQueryParams.getYearFilter().getOperation() != null) {
			switch(symbolFinancialsQueryParams.getYearFilter().getOperation()) {
			case SmallerEqual -> predicates.add(this.entityManager.getCriteriaBuilder().lessThanOrEqualTo(root.get("fiscalYear"),
					symbolFinancialsQueryParams.getYearFilter().getValue()));
			case LargerEqual -> predicates.add(this.entityManager.getCriteriaBuilder().greaterThanOrEqualTo(root.get("fiscalYear"),
					symbolFinancialsQueryParams.getYearFilter().getValue()));
			case Equal -> predicates.add(this.entityManager.getCriteriaBuilder().equal(root.get("fiscalYear"),
					symbolFinancialsQueryParams.getYearFilter().getValue()));
			}
		}
		return predicates;
	}

	private <T> void createFinancialElementClauses(List<FinancialElementParamDto> financialElementParamDtos,
			final Path<FinancialElement> fePath, final List<Predicate> predicates,
			final Optional<EntityType<SymbolFinancials>> symbolFinancialsOpt) {
		TermCollection termCollection = new TermCollection(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>());
		final LinkedBlockingQueue<TermCollection> subTermCollection = new LinkedBlockingQueue<TermCollection>();
		final LinkedBlockingQueue<DataHelper.Operation> operationArr = new LinkedBlockingQueue<DataHelper.Operation>();
		if (financialElementParamDtos != null) {
			financialElementParamDtos.forEach(myDto -> {
				switch (myDto.getTermType()) {
				case TermStart -> {
					try {
						operationArr.put(myDto.getOperation());
						subTermCollection.put(new TermCollection(new ArrayList<>(), new ArrayList<>(),
								new ArrayList<>(), new ArrayList<>()));
					} catch (InterruptedException e) {
						new RuntimeException(e);
					}
				}
				case Query -> {
					financialElementConceptClause(fePath,
							operationArr.isEmpty() ? termCollection : subTermCollection.peek(), myDto);
					financialElementValueClause(fePath,
							operationArr.isEmpty() ? termCollection : subTermCollection.peek(), myDto);
				}
				case TermEnd -> {
					Predicate myPredicate = createSubPredicate(operationArr, operationArr.isEmpty() ? termCollection : subTermCollection.poll());
					predicates.add(myPredicate);
				}
				}
			});
		}
		// validate terms
		if (!operationArr.isEmpty() || !subTermCollection.isEmpty()) {
			throw new RuntimeException(String.format("operationArr: %d, subPredicates: %d", operationArr.size(),
					subTermCollection.size()));
		}
		predicates.addAll(this.createTermCollectionPredicate(termCollection));
	}

	private Predicate createSubPredicate(final LinkedBlockingQueue<DataHelper.Operation> operationArr,
			final TermCollection termCollection) {
		Predicate myPredicate = switch (operationArr.poll()) {
		case And -> this.entityManager.getCriteriaBuilder()
				.and(this.createTermCollectionPredicate(termCollection).toArray(new Predicate[0]));
		case AndNot -> this.entityManager.getCriteriaBuilder().not(this.entityManager.getCriteriaBuilder()
				.and(this.createTermCollectionPredicate(termCollection).toArray(new Predicate[0])));
		case Or -> this.entityManager.getCriteriaBuilder()
				.or(this.createTermCollectionPredicate(termCollection).toArray(new Predicate[0]));
		case OrNot -> this.entityManager.getCriteriaBuilder().not(this.entityManager.getCriteriaBuilder()
				.or(this.createTermCollectionPredicate(termCollection).toArray(new Predicate[0])));
		};
		return myPredicate;
	}

	private Collection<Predicate> createTermCollectionPredicate(TermCollection termCollection) {
		List<Predicate> predicates = new ArrayList<>();
		if (!termCollection.and().isEmpty()) {
			predicates.add(this.entityManager.getCriteriaBuilder().and(termCollection.and().toArray(new Predicate[0])));
		} else if (!termCollection.andNot().isEmpty()) {
			predicates.add(this.entityManager.getCriteriaBuilder().not(
					this.entityManager.getCriteriaBuilder().and(termCollection.andNot().toArray(new Predicate[0]))));
		} else if (!termCollection.or().isEmpty()) {
			predicates.add(this.entityManager.getCriteriaBuilder().or(termCollection.or().toArray(new Predicate[0])));
		} else if (!termCollection.orNot().isEmpty()) {
			predicates.add(this.entityManager.getCriteriaBuilder()
					.not(this.entityManager.getCriteriaBuilder().or(termCollection.or().toArray(new Predicate[0]))));
		}
		return predicates;
	}

	private Set<FinancialElement> findFinancialElements(List<FinancialElementParamDto> financialElementParams) {
		final CriteriaQuery<FinancialElement> createQuery = this.entityManager.getCriteriaBuilder()
				.createQuery(FinancialElement.class);
		final Root<FinancialElement> root = createQuery.from(FinancialElement.class);
		root.fetch("symbolFinancials");
		final List<Predicate> predicates = new ArrayList<>();
		this.createFinancialElementClauses(financialElementParams, root, predicates, Optional.empty());
		if (!predicates.isEmpty()) {
			createQuery.where(predicates.toArray(new Predicate[0])).distinct(true);
		} else {
			return new HashSet<>();
		}
		return new HashSet<>(this.entityManager.createQuery(createQuery).setMaxResults(10000).getResultList());
	}

	private <T> void financialElementValueClause(Path<FinancialElement> fePath, TermCollection termCollection,
			FinancialElementParamDto myDto) {
		if (myDto.getValueFilter() != null && myDto.getValueFilter().getOperation() != null
				&& myDto.getValueFilter().getValue() != null
				&& (!BigDecimal.ZERO.equals(myDto.getValueFilter().getValue())
						&& !Operation.Equal.equals(myDto.getValueFilter().getOperation()))) {
			Expression<BigDecimal> joinPath = fePath.get("value");
			switch (myDto.getValueFilter().getOperation()) {
			case Equal -> this.operatorClause(termCollection, myDto.getOperation(),
					this.entityManager.getCriteriaBuilder().equal(joinPath, myDto.getValueFilter().getValue()));
			case SmallerEqual -> this.operatorClause(termCollection, myDto.getOperation(), this.entityManager
					.getCriteriaBuilder().lessThanOrEqualTo(joinPath, myDto.getValueFilter().getValue()));
			case LargerEqual -> this.operatorClause(termCollection, myDto.getOperation(), this.entityManager
					.getCriteriaBuilder().greaterThanOrEqualTo(joinPath, myDto.getValueFilter().getValue()));
			}
		}
	}

	private <T> void financialElementConceptClause(Path<FinancialElement> fePath, TermCollection termCollection,
			FinancialElementParamDto myDto) {
		if (myDto.getConceptFilter().getOperation() != null && myDto.getConceptFilter().getValue() != null
				&& myDto.getConceptFilter().getValue().trim().length() > 2) {
			Expression<String> lowerExp = this.entityManager.getCriteriaBuilder().lower(fePath.get("concept"));
			if (!myDto.getConceptFilter().getOperation()
					.equals(FilterStringDto.Operation.Equal)) {
				String filterStr = switch(myDto.getConceptFilter().getOperation()) {
				case Contains -> String.format("%%%s%%", myDto.getConceptFilter().getValue().trim().toLowerCase());
				case StartsWith -> String.format("%s%%", myDto.getConceptFilter().getValue().trim().toLowerCase());
				case EndsWith -> String.format("%%%s", myDto.getConceptFilter().getValue().trim().toLowerCase());
				default -> throw new IllegalArgumentException("Unexpected value: " + myDto.getConceptFilter().getOperation());
				};
				Predicate likePredicate = this.entityManager.getCriteriaBuilder().like(lowerExp, filterStr);
				operatorClause(termCollection, myDto.getOperation(), likePredicate);
			} else {
				Predicate equalPredicate = this.entityManager.getCriteriaBuilder().equal(lowerExp,
						myDto.getConceptFilter().getValue().trim().toLowerCase());
				this.operatorClause(termCollection, myDto.getOperation(), equalPredicate);
			}
		}
	}

	private void operatorClause(TermCollection termCollection, DataHelper.Operation operation, Predicate... likePredicate) {
		switch (operation) {
		case And -> termCollection.and().addAll(List.of(likePredicate));
		case AndNot -> termCollection.andNot().addAll(List.of(likePredicate));
		case Or -> termCollection.or().addAll(List.of(likePredicate));
		case OrNot -> termCollection.orNot().addAll(List.of(likePredicate));
		}
	}
}
