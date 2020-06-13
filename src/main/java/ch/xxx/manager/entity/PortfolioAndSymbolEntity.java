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

import org.springframework.data.relational.core.mapping.Table;

@Table
public class PortfolioAndSymbolEntity {
	private Long id;
	private Long userId;
	private String portfolioName;
	private LocalDate createdAt;
	private Long weight;
	private LocalDate changedAt;
	private LocalDate removedAt;
	private Long symbolId;
	private String symbol;
	private String symbolName;
	private String curr;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getPortfolioName() {
		return portfolioName;
	}
	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}
	public LocalDate getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}
	public Long getWeight() {
		return weight;
	}
	public void setWeight(Long weight) {
		this.weight = weight;
	}
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
	public Long getSymbolId() {
		return symbolId;
	}
	public void setSymbolId(Long symbolId) {
		this.symbolId = symbolId;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getSymbolName() {
		return symbolName;
	}
	public void setSymbolName(String symbolName) {
		this.symbolName = symbolName;
	}
	public String getCurr() {
		return curr;
	}
	public void setCurr(String curr) {
		this.curr = curr;
	}
	
	@Override
	public String toString() {
		return "PortfolioAndSymbolEntity [id=" + id + ", userId=" + userId + ", portfolioName=" + portfolioName
				+ ", createdAt=" + createdAt + ", weight=" + weight + ", changedAt=" + changedAt + ", removedAt="
				+ removedAt + ", symbolId=" + symbolId + ", symbol=" + symbol + ", symbolName=" + symbolName + ", curr="
				+ curr + "]";
	}
}
