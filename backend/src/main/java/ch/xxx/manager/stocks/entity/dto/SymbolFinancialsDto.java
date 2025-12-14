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
package ch.xxx.manager.stocks.entity.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import ch.xxx.manager.common.utils.DataHelper;

//@JsonInclude(value = Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown = true) 
public class SymbolFinancialsDto {
	private Long id;
	@JsonFormat(pattern="yyyy-MM-dd")
	private LocalDate startDate;
	@JsonFormat(pattern="yyyy-MM-dd")
	private LocalDate endDate;
	private int year;	
	private DataHelper.Quarter quarter;
	private String symbol;	
	private String name;
	private String country;
	private String city;
	private FinancialsDataDto data;	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public FinancialsDataDto getData() {
		return data;
	}
	public void setData(FinancialsDataDto data) {
		this.data = data;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	@Override
	public String toString() {
		return "SymbolFinancialsDto [id=" + id + ", startDate=" + startDate + ", endDate=" + endDate + ", year=" + year
				+ ", quarter=" + quarter + ", symbol=" + symbol + ", name=" + name + ", country=" + country + ", city="
				+ city + ", data=" + data + "]";
	}
}
