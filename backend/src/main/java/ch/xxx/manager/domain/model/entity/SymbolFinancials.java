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

import ch.xxx.manager.domain.utils.DataHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class SymbolFinancials extends EntityBase {
	private LocalDate startDate;
	private LocalDate endDate;
	@Column(name="`year`")
	private int year;
	@Enumerated(EnumType.STRING)
	private DataHelper.Quarter quarter;
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
	public DataHelper.Quarter getQuarter() {
		return quarter;
	}
	public void setQuarter(DataHelper.Quarter quarter) {
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
