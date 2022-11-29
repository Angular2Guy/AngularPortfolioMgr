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
package ch.xxx.manager.domain.model.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.xxx.manager.domain.utils.DataHelper.Quarter;

public class SymbolFinancialsDto {
	private LocalDate startDate;
	private LocalDate endDate;
	private int year;
	private Quarter quarter;
	private String symbol;
	private List<FinancialElementDto> financialElementDtos = new ArrayList<>();

	public List<FinancialElementDto> getFinancialElementDtos() {
		return financialElementDtos;
	}
	public void setFinancialElementDtos(List<FinancialElementDto> financialElementDtos) {
		this.financialElementDtos = financialElementDtos;
	}
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
}
