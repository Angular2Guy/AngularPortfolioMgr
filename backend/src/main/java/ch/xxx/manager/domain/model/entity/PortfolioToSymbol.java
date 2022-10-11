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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.PositiveOrZero;


@Entity
public class PortfolioToSymbol extends EntityBase {
	@ManyToOne
	private Portfolio portfolio;
	@ManyToOne
	private Symbol symbol;
	@PositiveOrZero
	private Long weight;
	private LocalDate changedAt;
	private LocalDate removedAt;
	
	public PortfolioToSymbol(Long id, Portfolio portfolio, Symbol symbol, Long weight, LocalDate changedAt,
			LocalDate removedAt) {
		super();
		super.setId(id);
		this.portfolio = portfolio;
		this.symbol = symbol;
		this.weight = weight;
		this.changedAt = changedAt;
		this.removedAt = removedAt;
	}
	
	public PortfolioToSymbol() {}

	public LocalDate getChangedAt() {
		return changedAt;
	}
	public void setChangedAt(LocalDate changedAt) {
		this.changedAt = changedAt;
	}
	public LocalDate getRemovedAt() {
		return removedAt;
	}
	public void setRemovedAt(LocalDate removedAt) {
		this.removedAt = removedAt;
	}
	public Long getWeight() {
		return weight;
	}
	public void setWeight(Long weight) {
		this.weight = weight;
	}
	public Portfolio getPortfolio() {
		return portfolio;
	}
	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}
	public Symbol getSymbol() {
		return symbol;
	}
	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}
}
