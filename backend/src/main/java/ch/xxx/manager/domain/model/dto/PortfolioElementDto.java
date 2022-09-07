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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonFormat;

import ch.xxx.manager.domain.utils.CurrencyKey;

public class PortfolioElementDto {
	private Long id;
	private String name;
	private String symbol;
	@Enumerated(EnumType.STRING)
	private CurrencyKey currencyKey;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime createdAt;
	private BigDecimal lastClose;
	private BigDecimal month1;
	private BigDecimal month6;
	private BigDecimal year1;
	private BigDecimal year2;
	private BigDecimal year5;
	private BigDecimal year10;
	private Long weight;
	private String sector;
	
	public PortfolioElementDto() {		
	}
	
	public PortfolioElementDto(Long id, String name, String symbol, CurrencyKey currencyKey, LocalDateTime createdAt,
			BigDecimal lastClose, BigDecimal month1, BigDecimal month6, BigDecimal year1, BigDecimal year2, BigDecimal year5,
			BigDecimal year10, Long weight, String sector) {
		super();
		this.id = id;
		this.name = name;
		this.symbol = symbol;
		this.currencyKey = currencyKey;
		this.createdAt = createdAt;
		this.lastClose = lastClose;
		this.month1 = month1;
		this.month6 = month6;
		this.year1 = year1;
		this.year2 = year2;
		this.year5 = year5;
		this.year10 = year10;
		this.weight = weight;
		this.sector = sector;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public CurrencyKey getCurrencyKey() {
		return currencyKey;
	}
	public void setCurrencyKey(CurrencyKey currencyKey) {
		this.currencyKey = currencyKey;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public BigDecimal getMonth1() {
		return month1;
	}
	public void setMonth1(BigDecimal month1) {
		this.month1 = month1;
	}
	public BigDecimal getMonth6() {
		return month6;
	}
	public void setMonth6(BigDecimal month6) {
		this.month6 = month6;
	}
	public BigDecimal getYear1() {
		return year1;
	}
	public void setYear1(BigDecimal year1) {
		this.year1 = year1;
	}
	public BigDecimal getYear2() {
		return year2;
	}
	public void setYear2(BigDecimal year2) {
		this.year2 = year2;
	}
	public BigDecimal getYear5() {
		return year5;
	}
	public void setYear5(BigDecimal year5) {
		this.year5 = year5;
	}
	public BigDecimal getYear10() {
		return year10;
	}
	public void setYear10(BigDecimal year10) {
		this.year10 = year10;
	}

	public BigDecimal getLastClose() {
		return lastClose;
	}

	public void setLastClose(BigDecimal lastClose) {
		this.lastClose = lastClose;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}	
}
