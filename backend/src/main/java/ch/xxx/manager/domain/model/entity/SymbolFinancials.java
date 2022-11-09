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
package ch.xxx.manager.domain.model.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
public class SymbolFinancials extends EntityBase {
	public static enum Quarter {
		H1("H1"), H2("H2"), H3("H3"), H4("H4"), T1("T1"), T2("T2"), T3("T3"), T4("T4"), CY("CY"), FY("FY"), Q1("Q1"), Q2("Q2"), Q3("Q3"), Q4("Q4");

		public final String value;

		private Quarter(String value) {
			this.value = value;
		}

		public String toString() {
			return this.value;
		}
	}
	private LocalDate startDate;
	private LocalDate endDate;
	@Column(name="`year`")
	private int year;
	@Enumerated(EnumType.STRING)
	private Quarter quarter;
	@NotBlank
	@Size(max=20)
	private String symbol;
	@OneToMany(mappedBy = "symbolFinancials")
	private Set<FinancialElement> balanceSheet = new HashSet<FinancialElement>();
	@OneToMany(mappedBy = "symbolFinancials")
	private Set<FinancialElement> cashFlow = new HashSet<FinancialElement>();
	@OneToMany(mappedBy = "symbolFinancials")
	private Set<FinancialElement> income = new HashSet<FinancialElement>();
	
	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public Quarter getQuarter() {
		return quarter;
	}
	public void setQuarter(Quarter quarter) {
		this.quarter = quarter;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Set<FinancialElement> getBalanceSheet() {
		return balanceSheet;
	}
	public void setBalanceSheet(Set<FinancialElement> balanceSheet) {
		this.balanceSheet = balanceSheet;
	}
	public Set<FinancialElement> getCashFlow() {
		return cashFlow;
	}
	public void setCashFlow(Set<FinancialElement> cashFlow) {
		this.cashFlow = cashFlow;
	}
	public Set<FinancialElement> getIncome() {
		return income;
	}
	public void setIncome(Set<FinancialElement> income) {
		this.income = income;
	}
}
