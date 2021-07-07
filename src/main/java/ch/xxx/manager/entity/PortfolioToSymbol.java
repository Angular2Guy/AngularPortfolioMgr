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
package ch.xxx.manager.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class PortfolioToSymbol {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	private Portfolio portfolio;
	private Symbol symbol;
	private Long weight;
	private LocalDate changedAt;
	private LocalDate removedAt;

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
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	
	@Override
	public String toString() {
		return "PortfolioToSymbol [id=" + id + ", portfolio=" + portfolio + ", symbol=" + symbol + ", weight=" + weight
				+ ", changedAt=" + changedAt + ", removedAt=" + removedAt + "]";
	}
}
