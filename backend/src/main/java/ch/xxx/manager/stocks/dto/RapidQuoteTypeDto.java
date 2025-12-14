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
package ch.xxx.manager.stocks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RapidQuoteTypeDto {
	private String exchange;
	private String shortName;
	private String longName;
	private String exchangeTimezoneName;
	private String exchangeTimezoneShortName;
	private Long gmtOffSetMilliseconds;
	private String quoteType;
	private String symbol;
	private String market;

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getExchangeTimezoneName() {
		return exchangeTimezoneName;
	}

	public void setExchangeTimezoneName(String exchangeTimezoneName) {
		this.exchangeTimezoneName = exchangeTimezoneName;
	}

	public String getExchangeTimezoneShortName() {
		return exchangeTimezoneShortName;
	}

	public void setExchangeTimezoneShortName(String exchangeTimezoneShortName) {
		this.exchangeTimezoneShortName = exchangeTimezoneShortName;
	}

	public Long getGmtOffSetMilliseconds() {
		return gmtOffSetMilliseconds;
	}

	public void setGmtOffSetMilliseconds(Long gmtOffSetMilliseconds) {
		this.gmtOffSetMilliseconds = gmtOffSetMilliseconds;
	}

	public String getQuoteType() {
		return quoteType;
	}

	public void setQuoteType(String quoteType) {
		this.quoteType = quoteType;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	@Override
	public String toString() {
		return "RapidQuoteType [exchange=" + exchange + ", shortName=" + shortName + ", longName=" + longName
				+ ", exchangeTimezoneName=" + exchangeTimezoneName + ", exchangeTimezoneShortName="
				+ exchangeTimezoneShortName + ", gmtOffSetMilliseconds=" + gmtOffSetMilliseconds + ", quoteType="
				+ quoteType + ", symbol=" + symbol + ", market=" + market + "]";
	}
}
