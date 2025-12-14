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
package ch.xxx.manager.stocks.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import ch.xxx.manager.common.utils.DataHelper;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@DiscriminatorValue("2")
public class PortfolioElement extends PortfolioBase {
	@NotBlank
	@Size(max=255)
	private String name;
	@NotBlank
	@Size(max=20)
	private String symbol;
	@NotNull
	@Enumerated(EnumType.STRING)
	private DataHelper.CurrencyKey currencyKey;
	@NotNull
	private LocalDate createdAt;
	@NotNull
	private BigDecimal lastClose;
	@PositiveOrZero
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
	public DataHelper.CurrencyKey getCurrencyKey() {
		return currencyKey;
	}
	public void setCurrencyKey(DataHelper.CurrencyKey currencyKey) {
		this.currencyKey = currencyKey;
	}
	public LocalDate getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
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

	public BigDecimal getLastClose() {
		return lastClose;
	}

	public void setLastClose(BigDecimal lastClose) {
		this.lastClose = lastClose;
	}
}
