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

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import ch.xxx.manager.domain.utils.CurrencyKey;

@Entity
@DiscriminatorValue("2")
public class PortfolioElement extends PortfolioBase {
	private String name;
	private String symbol;
	@Enumerated(EnumType.STRING)
	private CurrencyKey currencyKey;
	private LocalDate createdAt;
	private BigDecimal month1;
	private BigDecimal month6;
	private BigDecimal year1;
	private BigDecimal year2;
	private BigDecimal year5;
	private BigDecimal year10;
	private Long weight;
	private String sector;
	@ManyToOne
	private Portfolio portfolio;
	
	@PrePersist
	void init() {
		this.createdAt = LocalDate.now();
	}
	
	public Portfolio getPortfolio() {
		return portfolio;
	}
	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CurrencyKey getCurrencyKey() {
		return currencyKey;
	}
	public void setCurrencyKey(CurrencyKey currencyKey) {
		this.currencyKey = currencyKey;
	}
	public LocalDate getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDate createdAt) {
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

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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
