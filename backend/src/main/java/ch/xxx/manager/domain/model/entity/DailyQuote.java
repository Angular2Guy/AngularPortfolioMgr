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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import ch.xxx.manager.domain.utils.CurrencyKey;


@Entity
public class DailyQuote extends EntityBase {
	private String symbolKey;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private Long volume;
	private LocalDate localDay;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Symbol symbol;
	@Enumerated(EnumType.STRING)
	private CurrencyKey currencyKey;

	public DailyQuote() {
		super();
	}

	public DailyQuote(Long id, String symbolKey, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
			Long volume, LocalDate localDay, Symbol symbol, CurrencyKey currencyKey) {
		super();
		super.setId(id);
		this.symbolKey = symbolKey;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.localDay = localDay;
		this.symbol = symbol;
		this.currencyKey = currencyKey;
	}


	public LocalDate getLocalDay() {
		return localDay;
	}

	public void setLocalDay(LocalDate localDay) {
		this.localDay = localDay;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public String getSymbolKey() {
		return symbolKey;
	}

	public void setSymbolKey(String symbolKey) {
		this.symbolKey = symbolKey;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

	public CurrencyKey getCurrencyKey() {
		return currencyKey;
	}

	public void setCurrencyKey(CurrencyKey currencyKey) {
		this.currencyKey = currencyKey;
	}

	@Override
	public String toString() {
		return "DailyQuote [id=" + this.getId() + ", symbolKey=" + symbolKey + ", open=" + open + ", high=" + high + ", low="
				+ low + ", close=" + close + ", volume=" + volume + ", localDay=" + localDay + ", symbol=" + symbol
				+ ", currencyKey=" + currencyKey + "]";
	}

}
