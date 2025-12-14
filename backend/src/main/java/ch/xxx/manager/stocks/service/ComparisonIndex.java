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
package ch.xxx.manager.stocks.service;
import ch.xxx.manager.stocks.entity.Symbol.QuoteSource;
import ch.xxx.manager.common.utils.DataHelper;
import ch.xxx.manager.common.utils.DataHelper.CurrencyKey;

public enum ComparisonIndex {
	SP500("IVV", "S&P 500 ETF", DataHelper.CurrencyKey.USD, QuoteSource.YAHOO),
	EUROSTOXX50("SXRT.DE", "EuroStoxx 50 ETF", DataHelper.CurrencyKey.EUR, QuoteSource.YAHOO),
	MSCI_CHINA("ICGA.DE", "Msci China ETF", DataHelper.CurrencyKey.USD, QuoteSource.YAHOO);

	private String symbol;
	private String name;
	private CurrencyKey currencyKey;
	private QuoteSource source;

	private ComparisonIndex(String symbol, String name, DataHelper.CurrencyKey currency, QuoteSource source) {
		this.symbol = symbol;
		this.name = name;
		this.currencyKey = currency;
		this.source = source;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public String getName() {
		return name;
	}

	public DataHelper.CurrencyKey getCurrencyKey() {
		return currencyKey;
	}

	public QuoteSource getSource() {
		return source;
	}
};