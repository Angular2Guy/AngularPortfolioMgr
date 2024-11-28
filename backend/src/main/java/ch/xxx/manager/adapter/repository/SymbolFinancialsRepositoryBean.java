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
import ch.xxx.manager.domain.utils.StreamHelpers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class SymbolFinancialsRepositoryBean extends SymbolFinancialsRepositoryBaseBean
		implements SymbolFinancialsRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolFinancialsRepositoryBean.class);
	private static final String SYMBOL = "symbol";
	private static final String FINANCIAL_ELEMENTS = "financialElements";
	private static final String QUARTER = "quarter";
	private static final String FISCAL_YEAR = "fiscalYear";
	private static final String VALUE = "value";
	private static final String CONCEPT = "concept";
	private static final String NAME = "name";
	private static final String CITY = "city";
	private static final String COUNTRY = "country";
	private final EntityManager entityManager;

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
				&& (symbolFinancialsQueryParams.getCity() == null || symbolFinancialsQueryParams.getCity().isEmpty())
				&& (symbolFinancialsQueryParams.getCountry() == null
						|| symbolFinancialsQueryParams.getCountry().isEmpty())
				&& (symbolFinancialsQueryParams.getName() == null || symbolFinancialsQueryParams.getName().isEmpty())
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

		final List<Predicate> predicates = createSymbolFinancialsPredicates(symbolFinancialsQueryParams, root);

		predicates.addAll(this.limitYearQuarterResults(symbolFinancialsQueryParams, root));
		root.fetch(FINANCIAL_ELEMENTS);
		Path<FinancialElement> fePath = root.get(FINANCIAL_ELEMENTS);
		this.createFinancialElementClauses(symbolFinancialsQueryParams.getFinancialElementParams(), fePath, predicates);
		if (!predicates.isEmpty()) {
			createQuery.where(predicates.toArray(new Predicate[0])).distinct(true)
					.orderBy(this.entityManager.getCriteriaBuilder().asc(root.get(SYMBOL)));
		} else {
			return new LinkedList<>();
		}
		LocalTime start1 = LocalTime.now();
		result = this.entityManager.createQuery(createQuery).getResultStream()
				.map(mySymbolFinancials -> removeDublicates(mySymbolFinancials)).limit(100)
				.collect(Collectors.toList());
		LOGGER.info("Query1: {} ms", Duration.between(start1, LocalTime.now()).toMillis());
		return result;
	}

	private List<Predicate> limitYearQuarterResults(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams,
			final Root<SymbolFinancials> root) {
		List<Predicate> results = List.of();
		if ((symbolFinancialsQueryParams.getFinancialElementParams() == null
				|| symbolFinancialsQueryParams.getFinancialElementParams().isEmpty())
				&& (symbolFinancialsQueryParams.getSymbol() == null
						|| symbolFinancialsQueryParams.getSymbol().isBlank()) 
				&& (symbolFinancialsQueryParams.getName() == null || symbolFinancialsQueryParams.getName().isBlank())) {
			symbolFinancialsQueryParams.setSymbol("A");
			results = List.of(this.createColumnCriteria(symbolFinancialsQueryParams.getSymbol(), root, true, SYMBOL));
		}
		return results;
	}

	private SymbolFinancials removeDublicates(SymbolFinancials mySymbolFinancials) {
		this.entityManager.detach(mySymbolFinancials);
		List<FinancialElement> myfilteredFinancialElements = mySymbolFinancials.getFinancialElements().stream()
				.peek(myFinancialElement -> this.entityManager.detach(myFinancialElement))
				.filter(StreamHelpers.distinctByKey(myFinancialElement -> ""
						+ Optional.ofNullable(myFinancialElement.getConcept()).orElse("").trim()
						+ myFinancialElement.getCurrency() + myFinancialElement.getValue() != null
								? myFinancialElement.getValue().toString().trim()
								: ""))
				.collect(Collectors.toList());
		mySymbolFinancials.getFinancialElements().clear();
		mySymbolFinancials.getFinancialElements().addAll(myfilteredFinancialElements);
		return mySymbolFinancials;
	}

	private List<Predicate> createSymbolFinancialsPredicates(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams,
			final Root<SymbolFinancials> root) {
		final List<Predicate> predicates = new ArrayList<>();
		Optional.ofNullable(symbolFinancialsQueryParams.getSymbol()).stream()
				.filter(myValue -> !myValue.trim().isBlank()).forEach(myValue -> predicates
						.add(createColumnCriteria(symbolFinancialsQueryParams.getSymbol(), root, false, SYMBOL)));
		Optional.ofNullable(symbolFinancialsQueryParams.getName()).stream().map(String::trim).filter(java.util.function.Predicate.not(String::isBlank))
				.forEach(
						myValue -> predicates.add(createColumnCriteria(symbolFinancialsQueryParams.getName(), root, false, NAME)));
		Optional.ofNullable(symbolFinancialsQueryParams.getCity()).stream().map(String::trim).filter(java.util.function.Predicate.not(String::isBlank))
				.forEach(
						myValue -> predicates.add(createColumnCriteria(symbolFinancialsQueryParams.getCity(), root, false, CITY)));
		Optional.ofNullable(symbolFinancialsQueryParams.getCountry()).stream()				
				.map(String::trim).filter(java.util.function.Predicate.not(String::isBlank))
				.forEach(myValue -> predicates
						.add(createColumnCriteria(symbolFinancialsQueryParams.getCountry(), root, false, COUNTRY)));
		if (symbolFinancialsQueryParams.getQuarters() != null && !symbolFinancialsQueryParams.getQuarters().isEmpty()) {
			predicates.add(this.entityManager.getCriteriaBuilder().in(root.get(QUARTER))
					.value(symbolFinancialsQueryParams.getQuarters()));
		}
		if (symbolFinancialsQueryParams.getYearFilter() != null
				&& symbolFinancialsQueryParams.getYearFilter().getValue() != null
				&& 0 >= BigDecimal.valueOf(1800).compareTo(symbolFinancialsQueryParams.getYearFilter().getValue())
				&& symbolFinancialsQueryParams.getYearFilter().getOperation() != null) {
			switch (symbolFinancialsQueryParams.getYearFilter().getOperation()) {
			case SmallerEqual -> predicates.add(this.entityManager.getCriteriaBuilder()
					.lessThanOrEqualTo(root.get(FISCAL_YEAR), symbolFinancialsQueryParams.getYearFilter().getValue()));
			case LargerEqual ->
				predicates.add(this.entityManager.getCriteriaBuilder().greaterThanOrEqualTo(root.get(FISCAL_YEAR),
						symbolFinancialsQueryParams.getYearFilter().getValue()));
			case Equal -> predicates.add(this.entityManager.getCriteriaBuilder().equal(root.get(FISCAL_YEAR),
					symbolFinancialsQueryParams.getYearFilter().getValue()));
			}
		}
		return predicates;
	}

	private Predicate createColumnCriteria(String queryParamStr,
			final Root<SymbolFinancials> root, boolean uselike, String columnName) {
		Expression<String> lowerExpr = this.entityManager.getCriteriaBuilder().lower(root.get(columnName));
		String lowerStr = queryParamStr.trim().toLowerCase();
		return uselike ? this.entityManager.getCriteriaBuilder().like(lowerExpr, String.format("%s%%", lowerStr))
				: this.entityManager.getCriteriaBuilder().equal(lowerExpr, lowerStr);
	}

	private <T> void createFinancialElementClauses(List<FinancialElementParamDto> financialElementParamDtos,
			final Path<FinancialElement> fePath, final List<Predicate> predicates) {
		record SubTerm(DataHelper.Operation operation, Collection<Predicate> subTerms) {
		}
		final LinkedBlockingQueue<SubTerm> subTermQueue = new LinkedBlockingQueue<>();
		final Collection<Predicate> result = new LinkedList<>();
		if (financialElementParamDtos != null) {
			financialElementParamDtos.forEach(myDto -> {
				switch (myDto.getTermType()) {
				case TermStart -> {
					try {
						subTermQueue.put(new SubTerm(myDto.getOperation(), new ArrayList<>()));
					} catch (InterruptedException e) {
						new RuntimeException(e);
					}
				}
				case Query -> {
					Collection<Predicate> localResult = subTermQueue.isEmpty() ? result
							: subTermQueue.peek().subTerms();
					Optional<Predicate> conceptClauseOpt = financialElementConceptClause(fePath, myDto);
					Optional<Predicate> valueClauseOpt = financialElementValueClause(fePath, myDto);
					List<Predicate> myPredicates = List.of(conceptClauseOpt, valueClauseOpt).stream()
							.filter(Optional::isPresent).map(Optional::get).toList();
					if (myPredicates.size() > 1) {
						localResult.add(
								this.entityManager.getCriteriaBuilder().and(myPredicates.toArray(new Predicate[0])));
					} else {
						localResult.addAll(myPredicates);
					}
				}
				case TermEnd -> {
					if (subTermQueue.isEmpty()) {
						throw new RuntimeException(String.format("subPredicates: %d", subTermQueue.size()));
					}
					SubTerm subTermColl = subTermQueue.poll();
					Collection<Predicate> myPredicates = subTermColl.subTerms();
					Collection<Predicate> baseTermCollection = subTermQueue.peek() == null ? result
							: subTermQueue.peek().subTerms();
					DataHelper.Operation operation = subTermColl.operation();
					Collection<Predicate> resultPredicates = operation == null ? myPredicates : switch (operation) {
					case And ->
						List.of(this.entityManager.getCriteriaBuilder().and(myPredicates.toArray(new Predicate[0])));
					case AndNot -> List.of(this.entityManager.getCriteriaBuilder()
							.not(this.entityManager.getCriteriaBuilder().and(myPredicates.toArray(new Predicate[0]))));
					case Or ->
						List.of(this.entityManager.getCriteriaBuilder().or(myPredicates.toArray(new Predicate[0])));
					case OrNot -> List.of(this.entityManager.getCriteriaBuilder()
							.not(this.entityManager.getCriteriaBuilder().or(myPredicates.toArray(new Predicate[0]))));
					};
					baseTermCollection.addAll(resultPredicates);
				}
				}
			});
		}
		// validate terms
		if (!subTermQueue.isEmpty()) {
			throw new RuntimeException(String.format("subPredicates: %d", subTermQueue.size()));
		}
		predicates.addAll(result);
	}

	private Set<FinancialElement> findFinancialElements(List<FinancialElementParamDto> financialElementParams) {
		final CriteriaQuery<FinancialElement> createQuery = this.entityManager.getCriteriaBuilder()
				.createQuery(FinancialElement.class);
		final Root<FinancialElement> root = createQuery.from(FinancialElement.class);
		root.fetch("symbolFinancials");
		final List<Predicate> predicates = new ArrayList<>();
		this.createFinancialElementClauses(financialElementParams, root, predicates);
		if (!predicates.isEmpty()) {
			createQuery.where(predicates.toArray(new Predicate[0])).distinct(true);
		} else {
			return new HashSet<>();
		}
		return new HashSet<>(this.entityManager.createQuery(createQuery).setMaxResults(1000).getResultList());
	}

	private Optional<Predicate> financialElementValueClause(Path<FinancialElement> fePath,
			FinancialElementParamDto myDto) {
		Optional<Predicate> result = Optional.empty();
		if (myDto.getValueFilter() != null && myDto.getValueFilter().getOperation() != null
				&& myDto.getValueFilter().getValue() != null
				&& (!BigDecimal.ZERO.equals(myDto.getValueFilter().getValue())
						&& !Operation.Equal.equals(myDto.getValueFilter().getOperation()))) {
			Expression<BigDecimal> joinPath = fePath.get(VALUE);
			result = Optional.of(switch (myDto.getValueFilter().getOperation()) {
			case Equal -> this.entityManager.getCriteriaBuilder().equal(joinPath, myDto.getValueFilter().getValue());
			case SmallerEqual ->
				this.entityManager.getCriteriaBuilder().lessThanOrEqualTo(joinPath, myDto.getValueFilter().getValue());
			case LargerEqual -> this.entityManager.getCriteriaBuilder().greaterThanOrEqualTo(joinPath,
					myDto.getValueFilter().getValue());
			});
		}
		return result;
	}

	private Optional<Predicate> financialElementConceptClause(Path<FinancialElement> fePath,
			FinancialElementParamDto myDto) {
		Optional<Predicate> result = Optional.empty();
		if (myDto.getConceptFilter().getOperation() != null && myDto.getConceptFilter().getValue() != null
				&& myDto.getConceptFilter().getValue().trim().length() > 2) {
			Expression<String> lowerExp = this.entityManager.getCriteriaBuilder().lower(fePath.get(CONCEPT));
			if (!myDto.getConceptFilter().getOperation().equals(FilterStringDto.Operation.Equal)) {
				String filterStr = switch (myDto.getConceptFilter().getOperation()) {
				case Contains -> String.format("%%%s%%", myDto.getConceptFilter().getValue().trim().toLowerCase());
				case StartsWith -> String.format("%s%%", myDto.getConceptFilter().getValue().trim().toLowerCase());
				case EndsWith -> String.format("%%%s", myDto.getConceptFilter().getValue().trim().toLowerCase());
				default ->
					throw new IllegalArgumentException("Unexpected value: " + myDto.getConceptFilter().getOperation());
				};
				result = Optional.of(this.entityManager.getCriteriaBuilder().like(lowerExp, filterStr));
			} else {
				result = Optional.of(this.entityManager.getCriteriaBuilder().equal(lowerExp,
						myDto.getConceptFilter().getValue().trim().toLowerCase()));
			}
		}
		return result;
	}
}
