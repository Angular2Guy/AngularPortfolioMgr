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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ch.xxx.manager.utils.CurrencyKey;



@Entity
public class Symbol {
	public enum QuoteSource { ALPHAVANTAGE, YAHOO, PORTFOLIO }
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	private String symbol;
	private String name;
	private CurrencyKey currencyKey;
	private String source;
	
	
	public Symbol() {		
	}
	
	public Symbol(Long id, String symbol, String name, CurrencyKey currencyKey, QuoteSource quoteSource) {
		super();
		this.id = id;
		this.symbol = symbol;
		this.name = name;
		this.currencyKey = currencyKey;
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
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public CurrencyKey getCurrencyKey() {
		return currencyKey;
	}

	public void setCurrencyKey(CurrencyKey currencyKey) {
		this.currencyKey = currencyKey;
	}

	@Override
	public String toString() {
		return "Symbol [id=" + id + ", symbol=" + symbol + ", name=" + name + ", currencyKey=" + currencyKey
				+ ", source=" + source + "]";
	}
}
