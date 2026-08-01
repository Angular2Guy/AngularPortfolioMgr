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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

import ch.xxx.manager.common.utils.DataHelper;
import ch.xxx.manager.findata.dto.FilterNumberDto.Operation;
import ch.xxx.manager.findata.dto.FilterStringDto;
import ch.xxx.manager.findata.dto.FinancialElementParamDto;
import ch.xxx.manager.findata.entity.FinancialElement;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public final class FinancialElementSpecifications {
	private static final String CONCEPT = "concept";
	private static final String VALUE = "value";
	private static final String SYMBOL_FINANCIALS = "symbolFinancials";

	private FinancialElementSpecifications() {
	}

	public static Specification<FinancialElement> findByParams(List<FinancialElementParamDto> financialElementParams) {
		return (root, query, cb) -> {
			final List<Predicate> predicates = new ArrayList<>();
			createFinancialElementClauses(financialElementParams, root, cb, predicates);
			if (predicates.isEmpty()) {
				return cb.disjunction();
			}
			if (Long.class != query.getResultType()) {
				root.fetch(SYMBOL_FINANCIALS);
				query.distinct(true);
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	public static void createFinancialElementClauses(List<FinancialElementParamDto> financialElementParamDtos,
			final Path<FinancialElement> fePath, final CriteriaBuilder cb, final List<Predicate> predicates) {
		record SubTerm(DataHelper.Operation operation, Collection<Predicate> subTerms) {
		}
		final Deque<SubTerm> subTermStack = new ArrayDeque<>();
		final Collection<Predicate> result = new LinkedList<>();
		if (financialElementParamDtos != null) {
			financialElementParamDtos.forEach(myDto -> {
				switch (myDto.getTermType()) {
				case TermStart -> subTermStack.push(new SubTerm(myDto.getOperation(), new ArrayList<>()));
				case Query -> {
					Collection<Predicate> localResult = subTermStack.isEmpty() ? result
							: subTermStack.peek().subTerms();
					Optional<Predicate> conceptClauseOpt = financialElementConceptClause(fePath, myDto, cb);
					Optional<Predicate> valueClauseOpt = financialElementValueClause(fePath, myDto, cb);
					List<Predicate> myPredicates = List.of(conceptClauseOpt, valueClauseOpt).stream()
							.flatMap(Optional::stream).toList();
					if (myPredicates.size() > 1) {
						localResult.add(cb.and(myPredicates.toArray(new Predicate[0])));
					} else {
						localResult.addAll(myPredicates);
					}
				}
				case TermEnd -> {
					if (subTermStack.isEmpty()) {
						throw new RuntimeException(String.format("subPredicates: %d", subTermStack.size()));
					}
					SubTerm subTermColl = subTermStack.pop();
					Collection<Predicate> myPredicates = subTermColl.subTerms();
					Collection<Predicate> baseTermCollection = subTermStack.isEmpty() ? result
							: subTermStack.peek().subTerms();
					DataHelper.Operation operation = subTermColl.operation();
					Collection<Predicate> resultPredicates = operation == null ? myPredicates : switch (operation) {
					case And -> List.of(cb.and(myPredicates.toArray(new Predicate[0])));
					case AndNot -> List.of(cb.not(cb.and(myPredicates.toArray(new Predicate[0]))));
					case Or -> List.of(cb.or(myPredicates.toArray(new Predicate[0])));
					case OrNot -> List.of(cb.not(cb.or(myPredicates.toArray(new Predicate[0]))));
					};
					baseTermCollection.addAll(resultPredicates);
				}
				}
			});
		}
		// validate terms
		if (!subTermStack.isEmpty()) {
			throw new RuntimeException(String.format("subPredicates: %d", subTermStack.size()));
		}
		predicates.addAll(result);
	}

	private static Optional<Predicate> financialElementValueClause(Path<FinancialElement> fePath,
			FinancialElementParamDto myDto, CriteriaBuilder cb) {
		return Optional.ofNullable(myDto.getValueFilter()).filter(myFilter -> myFilter.getOperation() != null)
				.filter(myFilter -> myFilter.getValue() != null)
				.filter(myFilter -> !BigDecimal.ZERO.equals(myFilter.getValue()))
				.filter(myFilter -> !Operation.Equal.equals(myFilter.getOperation())).map(myFilter -> {
					Expression<BigDecimal> joinPath = fePath.get(VALUE);
					return switch (myFilter.getOperation()) {
					case Equal -> cb.equal(joinPath, myFilter.getValue());
					case SmallerEqual -> cb.lessThanOrEqualTo(joinPath, myFilter.getValue());
					case LargerEqual -> cb.greaterThanOrEqualTo(joinPath, myFilter.getValue());
					};
				});
	}

	private static Optional<Predicate> financialElementConceptClause(Path<FinancialElement> fePath,
			FinancialElementParamDto myDto, CriteriaBuilder cb) {
		Optional<Predicate> result = Optional.empty();
		if (myDto.getConceptFilter().getOperation() != null && myDto.getConceptFilter().getValue() != null
				&& myDto.getConceptFilter().getValue().trim().length() > 2) {
			Expression<String> lowerExp = cb.lower(fePath.get(CONCEPT));
			if (!myDto.getConceptFilter().getOperation().equals(FilterStringDto.Operation.Equal)) {
				String filterStr = switch (myDto.getConceptFilter().getOperation()) {
				case Contains -> String.format("%%%s%%", myDto.getConceptFilter().getValue().trim().toLowerCase());
				case StartsWith -> String.format("%s%%", myDto.getConceptFilter().getValue().trim().toLowerCase());
				case EndsWith -> String.format("%%%s", myDto.getConceptFilter().getValue().trim().toLowerCase());
				default ->
					throw new IllegalArgumentException("Unexpected value: " + myDto.getConceptFilter().getOperation());
				};
				result = Optional.of(cb.like(lowerExp, filterStr));
			} else {
				result = Optional.of(cb.equal(lowerExp, myDto.getConceptFilter().getValue().trim().toLowerCase()));
			}
		}
		return result;
	}
}
