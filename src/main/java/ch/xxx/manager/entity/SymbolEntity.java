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

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("symbol")
public class SymbolEntity {
	public enum SymbolCurrency { EUR, HKD, USD }
	public enum QuoteSource { ALPHAVANTAGE, YAHOO, PORTFOLIO }
	
	@Id
	private Long id;
	private String symbol;
	private String name;
	private String curr;
	private String source;
	
	
	public SymbolEntity() {		
	}
	
	public SymbolEntity(Long id, String symbol, String name, SymbolCurrency currency, QuoteSource quoteSource) {
		super();
		this.id = id;
		this.symbol = symbol;
		this.name = name;
		this.curr = currency.toString();
		this.source = quoteSource.toString();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCurr() {
		return curr;
	}
	public void setCurr(String currency) {
		this.curr = currency;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
