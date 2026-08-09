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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import ch.xxx.manager.common.utils.DataHelper;
import ch.xxx.manager.common.utils.DataHelper.CurrencyKey;
import ch.xxx.manager.common.utils.DataHelper.FinancialElementType;
import ch.xxx.manager.common.utils.DataHelper.Operation;
import ch.xxx.manager.common.utils.DataHelper.Quarter;
import ch.xxx.manager.common.utils.DataHelper.TermType;
import ch.xxx.manager.TestDataJpaConfig;
import ch.xxx.manager.findata.dto.FilterNumberDto;
import ch.xxx.manager.findata.dto.FilterStringDto;
import ch.xxx.manager.findata.dto.FinancialElementParamDto;
import ch.xxx.manager.findata.dto.SymbolFinancialsQueryParamsDto;
import ch.xxx.manager.findata.entity.FinancialElement;
import ch.xxx.manager.findata.entity.SymbolFinancials;

@DataJpaTest(showSql = false, properties = { "spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.liquibase.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.modulith.runtime.autoconfigure.SpringModulithRuntimeAutoConfiguration" })
@ContextConfiguration(classes = TestDataJpaConfig.class)
public class SymbolFinancialsRepositoryTest {
	@Autowired
	private SymbolFinancialsRepository symbolFinancialsRepository;
	@Autowired
	private JpaSymbolFinancialsRepository jpaSymbolFinancialsRepository;
	@Autowired
	private JpaFinancialElementRepository jpaFinancialElementRepository;

	// ----------------------------- helpers ---------------------------------

	private SymbolFinancials createSf(String symbol, String name, String city, String country, Quarter quarter,
			int fiscalYear) {
		SymbolFinancials sf = new SymbolFinancials();
		sf.setSymbol(symbol);
		sf.setName(name);
		sf.setCity(city);
		sf.setCountry(country);
		sf.setQuarter(quarter);
		sf.setFiscalYear(fiscalYear);
		sf.setStartDate(LocalDate.of(fiscalYear, 1, 1));
		sf.setEndDate(LocalDate.of(fiscalYear, 12, 31));
		return sf;
	}

	private FinancialElement createFe(SymbolFinancials sf, String concept, CurrencyKey currency, BigDecimal value) {
		FinancialElement fe = new FinancialElement();
		fe.setConcept(concept);
		fe.setLabel(concept);
		fe.setCurrency(currency);
		fe.setValue(value);
		fe.setFinancialElementType(FinancialElementType.Income);
		fe.setSymbolFinancials(sf);
		sf.getFinancialElements().add(fe);
		return fe;
	}

	private void persist(List<SymbolFinancials> sfList, List<FinancialElement> feList) {
		this.jpaSymbolFinancialsRepository.saveAll(sfList);
		this.jpaSymbolFinancialsRepository.flush();
		this.jpaFinancialElementRepository.saveAll(feList);
		this.jpaFinancialElementRepository.flush();
	}

	private void seedBaseData() {
		SymbolFinancials aapl = createSf("aapl", "Apple Inc.", "Cupertino", "US", Quarter.Q4, 2020);
		SymbolFinancials abb = createSf("abb", "ABB AG", "Zurich", "CH", Quarter.Q1, 2021);
		SymbolFinancials baba = createSf("baba", "Bayer AG", "Leverkusen", "DE", Quarter.Q2, 2019);
		SymbolFinancials sap = createSf("sap", "SAP SE", "Walldorf", "DE", Quarter.Q3, 2022);
		List<FinancialElement> fes = new ArrayList<>();
		fes.add(createFe(aapl, "Assets", CurrencyKey.USD, BigDecimal.valueOf(100)));
		fes.add(createFe(aapl, "Revenues", CurrencyKey.USD, BigDecimal.valueOf(200)));
		fes.add(createFe(aapl, "Debts", CurrencyKey.USD, BigDecimal.valueOf(50)));
		fes.add(createFe(abb, "Assets", CurrencyKey.USD, BigDecimal.valueOf(80)));
		fes.add(createFe(abb, "Revenues", CurrencyKey.USD, BigDecimal.valueOf(180)));
		fes.add(createFe(baba, "Assets", CurrencyKey.EUR, BigDecimal.valueOf(150)));
		fes.add(createFe(baba, "Revenues", CurrencyKey.EUR, BigDecimal.valueOf(250)));
		fes.add(createFe(baba, "Equities", CurrencyKey.EUR, BigDecimal.valueOf(90)));
		fes.add(createFe(sap, "Equities", CurrencyKey.EUR, BigDecimal.valueOf(5)));
		persist(List.of(aapl, abb, baba, sap), fes);
	}

	private void seedTermData() {
		SymbolFinancials tand1 = createSf("tand1", "Term And One", "Berlin", "DE", Quarter.Q1, 2020);
		SymbolFinancials tand2 = createSf("tand2", "Term And Two", "Berlin", "DE", Quarter.Q1, 2020);
		SymbolFinancials tor = createSf("tor", "Term Or", "Berlin", "DE", Quarter.Q1, 2020);
		SymbolFinancials tnot = createSf("tnot", "Term Not", "Berlin", "DE", Quarter.Q1, 2020);
		SymbolFinancials revonly = createSf("revonly", "Revenue Only", "Berlin", "DE", Quarter.Q1, 2020);
		List<FinancialElement> fes = new ArrayList<>();
		fes.add(createFe(tand1, "Revenues", CurrencyKey.USD, BigDecimal.valueOf(200)));
		fes.add(createFe(tand1, "Assets", CurrencyKey.USD, BigDecimal.valueOf(300)));
		fes.add(createFe(tand2, "Revenues", CurrencyKey.USD, BigDecimal.valueOf(100)));
		fes.add(createFe(tand2, "Assets", CurrencyKey.USD, BigDecimal.valueOf(50)));
		fes.add(createFe(tor, "Debts", CurrencyKey.USD, BigDecimal.valueOf(400)));
		fes.add(createFe(tnot, "Equities", CurrencyKey.USD, BigDecimal.valueOf(10)));
		fes.add(createFe(revonly, "Revenues", CurrencyKey.USD, BigDecimal.valueOf(500)));
		persist(List.of(tand1, tand2, tor, tnot, revonly), fes);
	}

	private void seedDuplicateData() {
		SymbolFinancials dup = createSf("dup", "Dup AG", "Berlin", "DE", Quarter.Q1, 2020);
		List<FinancialElement> fes = new ArrayList<>();
		fes.add(createFe(dup, "Assets", CurrencyKey.USD, BigDecimal.valueOf(100)));
		fes.add(createFe(dup, "Assets", CurrencyKey.USD, BigDecimal.valueOf(100)));
		fes.add(createFe(dup, "Assets", CurrencyKey.USD, BigDecimal.valueOf(200)));
		fes.add(createFe(dup, "Revenues", CurrencyKey.USD, BigDecimal.valueOf(50)));
		fes.add(createFe(dup, "Revenues", CurrencyKey.USD, BigDecimal.valueOf(50)));
		fes.add(createFe(dup, "Debts", CurrencyKey.USD, BigDecimal.valueOf(30)));
		persist(List.of(dup), fes);
	}

	private FilterStringDto conceptFilter(FilterStringDto.Operation operation, String value) {
		FilterStringDto dto = new FilterStringDto();
		dto.setOperation(operation);
		dto.setValue(value);
		return dto;
	}

	private FilterNumberDto numberFilter(FilterNumberDto.Operation operation, BigDecimal value) {
		FilterNumberDto dto = new FilterNumberDto();
		dto.setOperation(operation);
		dto.setValue(value);
		return dto;
	}

	private FinancialElementParamDto queryParam(FilterStringDto concept, FilterNumberDto value) {
		FinancialElementParamDto dto = new FinancialElementParamDto();
		dto.setConceptFilter(concept);
		dto.setValueFilter(value);
		dto.setTermType(TermType.Query);
		return dto;
	}

	private FinancialElementParamDto termStartParam(Operation operation) {
		FinancialElementParamDto dto = new FinancialElementParamDto();
		dto.setConceptFilter(conceptFilter(FilterStringDto.Operation.Contains, "xxx"));
		dto.setValueFilter(numberFilter(FilterNumberDto.Operation.Equal, BigDecimal.ONE));
		dto.setTermType(TermType.TermStart);
		dto.setOperation(operation);
		return dto;
	}

	private FinancialElementParamDto termStartNoOpParam() {
		FinancialElementParamDto dto = new FinancialElementParamDto();
		dto.setConceptFilter(conceptFilter(FilterStringDto.Operation.Contains, "xxx"));
		dto.setValueFilter(numberFilter(FilterNumberDto.Operation.Equal, BigDecimal.ONE));
		dto.setTermType(TermType.TermStart);
		return dto;
	}

	private FinancialElementParamDto termEndParam() {
		FinancialElementParamDto dto = new FinancialElementParamDto();
		dto.setConceptFilter(conceptFilter(FilterStringDto.Operation.Contains, "xxx"));
		dto.setValueFilter(numberFilter(FilterNumberDto.Operation.Equal, BigDecimal.ONE));
		dto.setTermType(TermType.TermEnd);
		return dto;
	}

	private SymbolFinancialsQueryParamsDto feParams(FinancialElementParamDto... params) {
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setFinancialElementParams(new ArrayList<>(Arrays.asList(params)));
		return dto;
	}

	private Set<String> symbolSet(List<SymbolFinancials> result) {
		return result.stream().map(SymbolFinancials::getSymbol).collect(Collectors.toSet());
	}

	private List<String> symbolList(List<SymbolFinancials> result) {
		return result.stream().map(SymbolFinancials::getSymbol).collect(Collectors.toList());
	}

	// ------------------- branch 1: financial element params only ------------

	@Test
	public void findByConceptContains() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "ssets"), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
		result.forEach(mySf -> mySf.getFinancialElements()
				.forEach(myFe -> Assertions.assertEquals("Assets", myFe.getConcept())));
	}

	@Test
	public void findByConceptStartsWith() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.StartsWith, "Ass"), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
	}

	@Test
	public void findByConceptEndsWith() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.EndsWith, "ties"), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("baba", "sap"), symbolSet(result));
	}

	@Test
	public void findByConceptEqual() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Equal, "Assets"), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
	}

	@Test
	public void findByConceptEqualIsCaseInsensitive() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Equal, "assets"), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
	}

	@Test
	public void findByConceptAndValueLargerEqual() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(queryParam(conceptFilter(FilterStringDto.Operation.Contains,
				"Revenu"), numberFilter(FilterNumberDto.Operation.LargerEqual, BigDecimal.valueOf(200))));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "baba"), symbolSet(result));
		result.forEach(mySf -> Assertions.assertEquals(1, mySf.getFinancialElements().size()));
	}

	@Test
	public void findByConceptAndValueSmallerEqual() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(queryParam(conceptFilter(FilterStringDto.Operation.Contains,
				"Revenu"), numberFilter(FilterNumberDto.Operation.SmallerEqual, BigDecimal.valueOf(200))));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb"), symbolSet(result));
	}

	@Test
	public void equalValueFilterIsIgnored() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(queryParam(conceptFilter(FilterStringDto.Operation.Contains,
				"Revenu"), numberFilter(FilterNumberDto.Operation.Equal, BigDecimal.valueOf(50))));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
	}

	@Test
	public void zeroValueFilterIsIgnored() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(queryParam(conceptFilter(FilterStringDto.Operation.Contains,
				"Revenu"), numberFilter(FilterNumberDto.Operation.LargerEqual, BigDecimal.ZERO)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
	}

	@Test
	public void nullValueFilterIsIgnored() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(queryParam(conceptFilter(FilterStringDto.Operation.Contains,
				"Revenu"), numberFilter(FilterNumberDto.Operation.LargerEqual, null)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
	}

	@Test
	public void nullOperationValueFilterIsIgnored() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(queryParam(conceptFilter(FilterStringDto.Operation.Contains,
				"Revenu"), numberFilter(null, BigDecimal.valueOf(200))));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
	}

	@Test
	public void conceptShorterThanThreeCharsReturnsEmpty() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(queryParam(conceptFilter(FilterStringDto.Operation.Contains,
				"Re"), numberFilter(FilterNumberDto.Operation.Equal, BigDecimal.valueOf(50))));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void findByConceptWithWhitespaceIsTrimmed() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "  Ass  "), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(Set.of("aapl", "abb", "baba"), symbolSet(result));
	}

	@Test
	public void findByNullConceptValueReturnsEmpty() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, null), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void financialElementResultsLimitedToThousand() {
		List<SymbolFinancials> sfList = new ArrayList<>();
		List<FinancialElement> feList = new ArrayList<>();
		for (int i = 0; i <= 1000; i++) {
			SymbolFinancials sf = createSf(String.format("c%04d", i), "Massive Data", "Berlin", "DE", Quarter.Q1,
					2020);
			sfList.add(sf);
			feList.add(createFe(sf, "Massive", CurrencyKey.USD, BigDecimal.valueOf(i)));
		}
		persist(sfList, feList);
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Massive"), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(1000, result.size());
	}

	// ------------------- branch 1: term operations --------------------------

	@Test
	public void findWithAndTerm() {
		seedTermData();
		List<FinancialElementParamDto> params = new ArrayList<>();
		params.add(termStartParam(Operation.And));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"),
				numberFilter(FilterNumberDto.Operation.SmallerEqual, BigDecimal.valueOf(150))));
		params.add(termEndParam());
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(feParams(params.toArray(new FinancialElementParamDto[0])));
		Assertions.assertEquals(Set.of("tand2"), symbolSet(result));
	}

	@Test
	public void findWithOrTerm() {
		seedTermData();
		List<FinancialElementParamDto> params = new ArrayList<>();
		params.add(termStartParam(Operation.Or));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Equal, "Debts"), null));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Equal, "Equities"), null));
		params.add(termEndParam());
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(feParams(params.toArray(new FinancialElementParamDto[0])));
		Assertions.assertEquals(Set.of("tor", "tnot"), symbolSet(result));
	}

	@Test
	public void findWithOrNotTerm() {
		seedTermData();
		List<FinancialElementParamDto> params = new ArrayList<>();
		params.add(termStartParam(Operation.OrNot));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Equal, "Debts"), null));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Equal, "Equities"), null));
		params.add(termEndParam());
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(feParams(params.toArray(new FinancialElementParamDto[0])));
		Assertions.assertEquals(Set.of("tand1", "tand2", "revonly"), symbolSet(result));
	}

	@Test
	public void findWithNullOperationTerm() {
		seedTermData();
		List<FinancialElementParamDto> params = new ArrayList<>();
		params.add(termStartNoOpParam());
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		params.add(termEndParam());
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(feParams(params.toArray(new FinancialElementParamDto[0])));
		Assertions.assertEquals(Set.of("tand1", "tand2", "revonly"), symbolSet(result));
	}

	@Test
	public void findWithAndNotTerm() {
		seedTermData();
		List<FinancialElementParamDto> params = new ArrayList<>();
		params.add(termStartParam(Operation.AndNot));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		params.add(termEndParam());
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(feParams(params.toArray(new FinancialElementParamDto[0])));
		Assertions.assertEquals(Set.of("tand1", "tand2", "tor", "tnot"), symbolSet(result));
	}

	@Test
	public void findWithNestedOrInAndTerm() {
		seedTermData();
		List<FinancialElementParamDto> params = new ArrayList<>();
		params.add(termStartParam(Operation.Or));
		params.add(termStartParam(Operation.And));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"),
				numberFilter(FilterNumberDto.Operation.LargerEqual, BigDecimal.valueOf(150))));
		params.add(termEndParam());
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Equal, "Equities"), null));
		params.add(termEndParam());
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(feParams(params.toArray(new FinancialElementParamDto[0])));
		Assertions.assertEquals(Set.of("tand1", "tnot", "revonly"), symbolSet(result));
	}

	@Test
	public void unbalancedTermStartThrows() {
		seedTermData();
		List<FinancialElementParamDto> params = new ArrayList<>();
		params.add(termStartParam(Operation.And));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
				() -> this.symbolFinancialsRepository.findSymbolFinancials(feParams(params.toArray(new FinancialElementParamDto[0]))));
		Assertions.assertEquals("subPredicates: 1", ex.getMessage());
	}

	@Test
	public void loneTermEndThrows() {
		seedTermData();
		RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
				() -> this.symbolFinancialsRepository.findSymbolFinancials(feParams(termEndParam())));
		Assertions.assertEquals("subPredicates: 0", ex.getMessage());
	}

	// ------------------- branch 2: symbol financials criteria ---------------

	@Test
	public void emptyParamsFallBackToSymbolStartingWithA() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl", "abb"), symbolList(result));
		Assertions.assertEquals(Set.of("Assets", "Revenues", "Debts"),
				result.get(0).getFinancialElements().stream().map(FinancialElement::getConcept)
						.collect(Collectors.toSet()));
		Assertions.assertEquals(Set.of("Assets", "Revenues"),
				result.get(1).getFinancialElements().stream().map(FinancialElement::getConcept)
						.collect(Collectors.toSet()));
	}

	@Test
	public void nullFinancialElementParamsFallBackToSymbolStartingWithA() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setFinancialElementParams(null);
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl", "abb"), symbolList(result));
	}

	@Test
	public void findBySymbol() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("aapl");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl"), symbolList(result));
	}

	@Test
	public void findBySymbolIsCaseInsensitive() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("AAPL");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl"), symbolList(result));
	}

	@Test
	public void findBySymbolIsTrimmed() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("  aapl  ");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl"), symbolList(result));
	}

	@Test
	public void findByUnknownSymbolReturnsEmpty() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("zzzz");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void findByName() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setName("Bayer AG");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("baba"), symbolList(result));
	}

	@Test
	public void findByNameIsCaseInsensitive() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setName("  bayer ag  ");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("baba"), symbolList(result));
	}

	@Test
	public void findByCity() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setCity("Zurich");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("abb"), symbolList(result));
	}

	@Test
	public void findByCountry() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setCountry("DE");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("baba", "sap"), symbolList(result));
	}

	@Test
	public void findByQuarters() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setQuarters(List.of(Quarter.Q1));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("abb"), symbolList(result));
	}

	@Test
	public void findByMultipleQuarters() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setQuarters(List.of(Quarter.Q2, Quarter.Q3));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("baba", "sap"), symbolList(result));
	}

	@Test
	public void findByFiscalYearEqual() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setYearFilter(numberFilter(FilterNumberDto.Operation.Equal, BigDecimal.valueOf(2020)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl"), symbolList(result));
	}

	@Test
	public void findByFiscalYearSmallerEqual() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setYearFilter(numberFilter(FilterNumberDto.Operation.SmallerEqual, BigDecimal.valueOf(2020)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl", "baba"), symbolList(result));
	}

	@Test
	public void findByFiscalYearLargerEqual() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setYearFilter(numberFilter(FilterNumberDto.Operation.LargerEqual, BigDecimal.valueOf(2021)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("abb", "sap"), symbolList(result));
	}

	@Test
	public void invalidYearFilterIsIgnored() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setYearFilter(numberFilter(FilterNumberDto.Operation.SmallerEqual, BigDecimal.valueOf(1700)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl", "abb"), symbolList(result));
	}

	@Test
	public void yearFilterWithNullOperationFallsBackToASymbol() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setYearFilter(numberFilter(null, BigDecimal.valueOf(2020)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl", "abb"), symbolList(result));
	}

	@Test
	public void yearFilterWithNullValueFallsBackToASymbol() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setYearFilter(numberFilter(FilterNumberDto.Operation.LargerEqual, null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl", "abb"), symbolList(result));
	}

	@Test
	public void findWithSymbolAndYearCombined() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("baba");
		dto.setYearFilter(numberFilter(FilterNumberDto.Operation.Equal, BigDecimal.valueOf(2019)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("baba"), symbolList(result));
	}

	@Test
	public void findWithSymbolAndNonMatchingQuarterReturnsEmpty() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("aapl");
		dto.setQuarters(List.of(Quarter.Q3));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void findWithFinancialElementParamsAndYearFilter() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		dto.setYearFilter(numberFilter(FilterNumberDto.Operation.LargerEqual, BigDecimal.valueOf(2020)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl", "abb"), symbolList(result));
	}

	@Test
	public void findWithFinancialElementParamsAndSymbol() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		dto.setSymbol("baba");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("baba"), symbolList(result));
	}

	@Test
	public void findWithFinancialElementParamsAndQuarters() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		dto.setQuarters(List.of(Quarter.Q1));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("abb"), symbolList(result));
	}

	@Test
	public void findWithFinancialElementParamsAndCity() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		dto.setCity("Cupertino");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl"), symbolList(result));
	}

	@Test
	public void findWithFinancialElementParamsAndCountry() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		dto.setCountry("DE");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("baba"), symbolList(result));
	}

	@Test
	public void findWithFinancialElementParamsAndName() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"), null));
		dto.setName("Bayer AG");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("baba"), symbolList(result));
	}

	@Test
	public void findWithAllCriteriaCombined() {
		seedBaseData();
		List<FinancialElementParamDto> params = new ArrayList<>();
		params.add(termStartParam(Operation.And));
		params.add(queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Revenu"),
				numberFilter(FilterNumberDto.Operation.LargerEqual, BigDecimal.valueOf(200))));
		params.add(termEndParam());
		SymbolFinancialsQueryParamsDto dto = feParams(params.toArray(new FinancialElementParamDto[0]));
		dto.setSymbol("aapl");
		dto.setName("Apple Inc.");
		dto.setCity("Cupertino");
		dto.setCountry("US");
		dto.setQuarters(List.of(Quarter.Q4));
		dto.setYearFilter(numberFilter(FilterNumberDto.Operation.Equal, BigDecimal.valueOf(2020)));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(List.of("aapl"), symbolList(result));
		SymbolFinancials aapl = result.get(0);
		Assertions.assertEquals(Quarter.Q4, aapl.getQuarter());
		Assertions.assertEquals(2020, aapl.getFiscalYear());
		Assertions.assertTrue(aapl.getFinancialElements().stream()
				.anyMatch(fe -> "Revenues".equals(fe.getConcept())
						&& BigDecimal.valueOf(200).compareTo(fe.getValue()) == 0));
	}

	@Test
	public void findWithFinancialElementParamsAndNonMatchingSymbolReturnsEmpty() {
		seedBaseData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Equit"), null));
		dto.setSymbol("aapl");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void branch2DeduplicatesFinancialElements() {
		seedDuplicateData();
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("dup");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(1, result.size());
		Set<String> conceptValuePairs = result.get(0).getFinancialElements().stream()
				.map(myFe -> myFe.getConcept() + myFe.getValue()).collect(Collectors.toSet());
		Assertions.assertEquals(Set.of("Assets100", "Assets200", "Revenues50", "Debts30"), conceptValuePairs);
	}

	@Test
	public void deduplicationHandlesNullConceptAndValue() {
		SymbolFinancials nulls = createSf("nulls", "Null AG", "Berlin", "DE", Quarter.Q1, 2020);
		List<FinancialElement> fes = new ArrayList<>();
		fes.add(createFe(nulls, null, CurrencyKey.USD, null));
		fes.add(createFe(nulls, null, CurrencyKey.USD, null));
		fes.add(createFe(nulls, "Assets", CurrencyKey.USD, null));
		fes.add(createFe(nulls, null, CurrencyKey.USD, BigDecimal.valueOf(100)));
		persist(List.of(nulls), fes);
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("nulls");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(1, result.size());
		Set<String> conceptValuePairs = result.get(0).getFinancialElements().stream()
				.map(myFe -> myFe.getConcept() + "-" + myFe.getValue()).collect(Collectors.toSet());
		Assertions.assertEquals(Set.of("null-null", "Assets-null", "null-100"), conceptValuePairs);
	}

	@Test
	public void branch1DoesNotDeduplicateFinancialElements() {
		seedDuplicateData();
		SymbolFinancialsQueryParamsDto dto = feParams(
				queryParam(conceptFilter(FilterStringDto.Operation.Contains, "Assets"), null));
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals(3, result.get(0).getFinancialElements().size());
		long valueHundredCount = result.get(0).getFinancialElements().stream()
				.filter(myFe -> myFe.getValue().compareTo(BigDecimal.valueOf(100)) == 0).count();
		Assertions.assertEquals(2, valueHundredCount);
	}

	@Test
	public void symbolWithoutFinancialElementsIsExcluded() {
		SymbolFinancials nofe = createSf("nofe", "No Fe", "Berlin", "DE", Quarter.Q1, 2020);
		this.jpaSymbolFinancialsRepository.saveAndFlush(nofe);
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setSymbol("nofe");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void resultLimitedToHundred() {
		List<SymbolFinancials> sfList = new ArrayList<>();
		List<FinancialElement> feList = new ArrayList<>();
		for (int i = 1; i <= 120; i++) {
			SymbolFinancials sf = createSf(String.format("b%04d", i), "Limit Test", "Berlin", "DE", Quarter.Q1, 2020);
			sfList.add(sf);
			feList.add(createFe(sf, "Revenues", CurrencyKey.USD, BigDecimal.valueOf(i)));
		}
		persist(sfList, feList);
		SymbolFinancialsQueryParamsDto dto = new SymbolFinancialsQueryParamsDto();
		dto.setName("Limit Test");
		List<SymbolFinancials> result = this.symbolFinancialsRepository.findSymbolFinancials(dto);
		Assertions.assertEquals(100, result.size());
		Assertions.assertEquals("b0001", result.get(0).getSymbol());
		Assertions.assertEquals("b0100", result.get(99).getSymbol());
	}
}
