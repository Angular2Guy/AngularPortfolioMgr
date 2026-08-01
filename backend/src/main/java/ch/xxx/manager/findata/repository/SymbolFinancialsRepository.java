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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.findata.dto.FilterNumberDto.Operation;
import ch.xxx.manager.findata.dto.FinancialElementParamDto;
import ch.xxx.manager.findata.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.findata.entity.FinancialElement;
import ch.xxx.manager.findata.entity.SymbolFinancials;
import ch.xxx.manager.common.utils.StreamHelpers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class SymbolFinancialsRepository extends SymbolFinancialsRepositoryBaseBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolFinancialsRepository.class);
	private static final String SYMBOL = "symbol";
	private static final String FINANCIAL_ELEMENTS = "financialElements";
	private static final String QUARTER = "quarter";
	private static final String FISCAL_YEAR = "fiscalYear";
	private static final String NAME = "name";
	private static final String CITY = "city";
	private static final String COUNTRY = "country";
	private static final int MAX_RESULTS = 1000;
	private final JpaFinancialElementRepository jpaFinancialElementRepository;
	private final EntityManager entityManager;

	public SymbolFinancialsRepository(JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository,
			JpaFinancialElementRepository jpaFinancialElementRepository, EntityManager entityManager) {
		super(jpaSymbolFinancialsRepository);
		this.jpaFinancialElementRepository = jpaFinancialElementRepository;
		this.entityManager = entityManager;
	}

	public List<SymbolFinancials> findSymbolFinancials(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		if (this.isFinancialElementOnlyQuery(symbolFinancialsQueryParams)) {
			return this.findSymbolFinancialsByFinancialElements(symbolFinancialsQueryParams.getFinancialElementParams());
		}
		final Specification<SymbolFinancials> specification = this
				.createSymbolFinancialsSpecification(symbolFinancialsQueryParams);
		LocalTime start1 = LocalTime.now();
		final List<SymbolFinancials> result = this.jpaSymbolFinancialsRepository.findAll(specification).stream()
				.map(this::removeDublicates).limit(100).collect(Collectors.toList());
		LOGGER.info("Query1: {} ms", Duration.between(start1, LocalTime.now()).toMillis());
		return result;
	}

	private List<SymbolFinancials> findSymbolFinancialsByFinancialElements(
			List<FinancialElementParamDto> financialElementParams) {
		LocalTime start1 = LocalTime.now();
		final List<FinancialElement> financialElements = this.jpaFinancialElementRepository
				.findAll(FinancialElementSpecifications.findByParams(financialElementParams),
						PageRequest.of(0, MAX_RESULTS))
				.getContent();
		financialElements.forEach(myFe -> {
			this.entityManager.detach(myFe);
			this.entityManager.detach(myFe.getSymbolFinancials());
		});
		final Map<SymbolFinancials, Set<FinancialElement>> sfToFeMap = financialElements.stream()
				.collect(Collectors.groupingBy(FinancialElement::getSymbolFinancials, Collectors.toSet()));
		sfToFeMap.forEach(SymbolFinancials::setFinancialElements);
		LOGGER.info("Query1: {} ms", Duration.between(start1, LocalTime.now()).toMillis());
		return new ArrayList<>(sfToFeMap.keySet());
	}

	private boolean isFinancialElementOnlyQuery(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		return Optional.ofNullable(symbolFinancialsQueryParams.getFinancialElementParams())
				.filter(myParams -> !myParams.isEmpty()).isPresent()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getSymbol())
						.filter(mySymbol -> !mySymbol.isBlank()).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getQuarters())
						.filter(myQuarters -> !myQuarters.isEmpty()).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getCity())
						.filter(myCity -> !myCity.isEmpty()).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getCountry())
						.filter(myCountry -> !myCountry.isEmpty()).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getName())
						.filter(myName -> !myName.isEmpty()).isEmpty()
				&& this.isYearFilterInvalidOrAbsent(symbolFinancialsQueryParams);
	}

	private boolean isYearFilterInvalidOrAbsent(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		return Optional.ofNullable(symbolFinancialsQueryParams.getYearFilter())
				.filter(myFilter -> myFilter.getValue() != null)
				.filter(myFilter -> 0 >= BigDecimal.valueOf(1800).compareTo(myFilter.getValue()))
				.filter(myFilter -> myFilter.getOperation() != null).isEmpty();
	}

	private Specification<SymbolFinancials> createSymbolFinancialsSpecification(
			SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams) {
		return (root, query, cb) -> {
			final List<Predicate> predicates = new ArrayList<>();
			this.createSymbolFinancialsPredicates(symbolFinancialsQueryParams, root, cb, predicates);
			predicates.addAll(this.limitYearQuarterResults(symbolFinancialsQueryParams, root, cb));
			root.fetch(FINANCIAL_ELEMENTS);
			Path<FinancialElement> fePath = root.get(FINANCIAL_ELEMENTS);
			FinancialElementSpecifications.createFinancialElementClauses(
					symbolFinancialsQueryParams.getFinancialElementParams(), fePath, cb, predicates);
			if (predicates.isEmpty()) {
				return cb.disjunction();
			}
			query.distinct(true);
			query.orderBy(cb.asc(root.get(SYMBOL)));
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private List<Predicate> limitYearQuarterResults(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams,
			final Root<SymbolFinancials> root, final CriteriaBuilder cb) {
		List<Predicate> results = List.of();
		if (Optional.ofNullable(symbolFinancialsQueryParams.getFinancialElementParams())
				.filter(myParams -> !myParams.isEmpty()).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getSymbol())
						.filter(java.util.function.Predicate.not(String::isBlank)).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getName())
						.filter(java.util.function.Predicate.not(String::isBlank)).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getQuarters())
						.filter(myQuarters -> !myQuarters.isEmpty()).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getCity())
						.filter(java.util.function.Predicate.not(String::isBlank)).isEmpty()
				&& Optional.ofNullable(symbolFinancialsQueryParams.getCountry())
						.filter(java.util.function.Predicate.not(String::isBlank)).isEmpty()
				&& this.isYearFilterInvalidOrAbsent(symbolFinancialsQueryParams)) {
			results = List.of(this.createColumnCriteria("A", root, true, SYMBOL, cb));
		}
		return results;
	}

	private SymbolFinancials removeDublicates(SymbolFinancials mySymbolFinancials) {
		this.entityManager.detach(mySymbolFinancials);
		List<FinancialElement> myfilteredFinancialElements = mySymbolFinancials.getFinancialElements().stream()
				.peek(this.entityManager::detach)
				.filter(StreamHelpers.distinctByKey(myFinancialElement -> ""
						+ Optional.ofNullable(myFinancialElement.getConcept()).orElse("").trim()
						+ myFinancialElement.getCurrency()
						+ (myFinancialElement.getValue() != null ? myFinancialElement.getValue().toString().trim()
								: "")))
				.toList();
		mySymbolFinancials.getFinancialElements().clear();
		mySymbolFinancials.getFinancialElements().addAll(myfilteredFinancialElements);
		return mySymbolFinancials;
	}

	private void createSymbolFinancialsPredicates(SymbolFinancialsQueryParamsDto symbolFinancialsQueryParams,
			final Root<SymbolFinancials> root, final CriteriaBuilder cb, final List<Predicate> predicates) {
		Optional.ofNullable(symbolFinancialsQueryParams.getSymbol()).stream()
				.filter(myValue -> !myValue.trim().isBlank()).forEach(myValue -> predicates
						.add(createColumnCriteria(symbolFinancialsQueryParams.getSymbol(), root, false, SYMBOL, cb)));
		Optional.ofNullable(symbolFinancialsQueryParams.getName()).stream().map(String::trim)
				.filter(java.util.function.Predicate.not(String::isBlank)).forEach(myValue -> predicates
						.add(createColumnCriteria(symbolFinancialsQueryParams.getName(), root, false, NAME, cb)));
		Optional.ofNullable(symbolFinancialsQueryParams.getCity()).stream().map(String::trim)
				.filter(java.util.function.Predicate.not(String::isBlank)).forEach(myValue -> predicates
						.add(createColumnCriteria(symbolFinancialsQueryParams.getCity(), root, false, CITY, cb)));
		Optional.ofNullable(symbolFinancialsQueryParams.getCountry()).stream().map(String::trim)
				.filter(java.util.function.Predicate.not(String::isBlank)).forEach(myValue -> predicates
						.add(createColumnCriteria(symbolFinancialsQueryParams.getCountry(), root, false, COUNTRY, cb)));
		if (symbolFinancialsQueryParams.getQuarters() != null && !symbolFinancialsQueryParams.getQuarters().isEmpty()) {
			predicates.add(cb.in(root.get(QUARTER)).value(symbolFinancialsQueryParams.getQuarters()));
		}
		if (symbolFinancialsQueryParams.getYearFilter() != null
				&& symbolFinancialsQueryParams.getYearFilter().getValue() != null
				&& 0 >= BigDecimal.valueOf(1800).compareTo(symbolFinancialsQueryParams.getYearFilter().getValue())
				&& symbolFinancialsQueryParams.getYearFilter().getOperation() != null) {
			switch (symbolFinancialsQueryParams.getYearFilter().getOperation()) {
			case SmallerEqual -> predicates.add(cb.lessThanOrEqualTo(root.get(FISCAL_YEAR),
					symbolFinancialsQueryParams.getYearFilter().getValue()));
			case LargerEqual ->
				predicates.add(cb.greaterThanOrEqualTo(root.get(FISCAL_YEAR),
						symbolFinancialsQueryParams.getYearFilter().getValue()));
			case Equal -> predicates.add(
					cb.equal(root.get(FISCAL_YEAR), symbolFinancialsQueryParams.getYearFilter().getValue()));
			}
		}
	}

	private Predicate createColumnCriteria(String queryParamStr, final Root<SymbolFinancials> root, boolean uselike,
			String columnName, CriteriaBuilder cb) {
		Expression<String> lowerExpr = cb.lower(root.get(columnName));
		String lowerStr = queryParamStr.trim().toLowerCase();
		return uselike ? cb.like(lowerExpr, String.format("%s%%", lowerStr))
				: cb.equal(lowerExpr, lowerStr);
	}
}
