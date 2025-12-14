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
package ch.xxx.manager.findata.dto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.xxx.manager.common.utils.DataHelper;
import ch.xxx.manager.common.utils.DataHelper.Quarter;

public class SymbolFinancialsQueryParamsDto {
	private FilterNumberDto yearFilter;
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	@JsonProperty
	private List<Quarter> quarters = new LinkedList<>();
	private String symbol;
	private String name;
	private String city;
	private String country;
	private List<FinancialElementParamDto> financialElementParams = new ArrayList<>();
	
	public FilterNumberDto getYearFilter() {
		return yearFilter;
	}
	public void setYearFilter(FilterNumberDto yearFilter) {
		this.yearFilter = yearFilter;
	}
	public List<DataHelper.Quarter> getQuarters() {
		return quarters;
	}
	public void setQuarters(List<DataHelper.Quarter> quarter) {
		this.quarters = quarter;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public List<FinancialElementParamDto> getFinancialElementParams() {
		return financialElementParams;
	}
	public void setFinancialElementParams(List<FinancialElementParamDto> financialElementParams) {
		this.financialElementParams = financialElementParams;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
}
