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
package ch.xxx.manager.service;
import ch.xxx.manager.entity.SymbolEntity.QuoteSource;
import ch.xxx.manager.entity.SymbolEntity.SymbolCurrency;
import ch.xxx.manager.service.ServiceUtils.RefMarker;

public enum ComparisonIndexes {
	SP500("IVV", "S&P 500 ETF", SymbolCurrency.USD, QuoteSource.ALPHAVANTAGE, RefMarker.US_REF_MARKER),
	EUROSTOXX50("SXRT.DE", "EuroStoxx 50 ETF", SymbolCurrency.EUR, QuoteSource.ALPHAVANTAGE, RefMarker.EUROPE_REF_MARKER),
	MSCI_CHINA("ICGA.DE", "Msci China ETF", SymbolCurrency.USD, QuoteSource.ALPHAVANTAGE, RefMarker.CHINA_REF_MARKER);

	private String symbol;
	private String name;
	private SymbolCurrency currency;
	private QuoteSource source;
	private RefMarker refMarker;

	private ComparisonIndexes(String symbol, String name, SymbolCurrency currency, QuoteSource source, RefMarker refMarker) {
		this.symbol = symbol;
		this.name = name;
		this.currency = currency;
		this.source = source;
		this.refMarker = refMarker;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public String getName() {
		return name;
	}

	public SymbolCurrency getCurrency() {
		return currency;
	}

	public QuoteSource getSource() {
		return source;
	}

	public RefMarker getRefMarker() {
		return refMarker;
	}

};