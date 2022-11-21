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
package ch.xxx.manager.domain.model.entity.dto;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FinancialsDataDto {
	@JsonProperty(value = "bs")
	private Set<FinancialElementImportDto> balanceSheet = new HashSet<>();
	@JsonProperty(value = "cf")
	private Set<FinancialElementImportDto> cashFlow = new HashSet<>();
	@JsonProperty(value = "ic")
	private Set<FinancialElementImportDto> income = new HashSet<>();
	
	public Set<FinancialElementImportDto> getBalanceSheet() {
		return balanceSheet;
	}
	public void setBalanceSheet(Set<FinancialElementImportDto> balanceSheet) {
		this.balanceSheet = balanceSheet;
	}
	public Set<FinancialElementImportDto> getCashFlow() {
		return cashFlow;
	}
	public void setCashFlow(Set<FinancialElementImportDto> cashFlow) {
		this.cashFlow = cashFlow;
	}
	public Set<FinancialElementImportDto> getIncome() {
		return income;
	}
	public void setIncome(Set<FinancialElementImportDto> income) {
		this.income = income;
	}

	@Override
	public String toString() {
		return "FinancialsDataDto [balanceSheet=" + balanceSheet + ", cashFlow=" + cashFlow + ", income=" + income
				+ "]";
	}
}
