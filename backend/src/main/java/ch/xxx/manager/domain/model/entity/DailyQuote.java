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

import ch.xxx.manager.domain.utils.DataHelper;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
public class DailyQuote extends EntityBase {
	@NotBlank
	@Size(max=20)
	private String symbolKey;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	@NotNull
	private BigDecimal close;
	@NotNull
	private BigDecimal adjClose;
	private Long volume;
	@NotNull
	private LocalDate localDay;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Symbol symbol;
	@NotNull
	@Enumerated(EnumType.STRING)
	private DataHelper.CurrencyKey currencyKey;
	private BigDecimal split;
	private BigDecimal dividend;

	public DailyQuote() {
		super();
	}

	public DailyQuote(Long id, String symbolKey, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal adjClose,
			Long volume, LocalDate localDay, Symbol symbol, DataHelper.CurrencyKey currencyKey, BigDecimal split, BigDecimal dividend) {
		super();
		super.setId(id);
		this.symbolKey = symbolKey;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.adjClose = adjClose;
		this.volume = volume;
		this.localDay = localDay;
		this.symbol = symbol;
		this.currencyKey = currencyKey;
		this.split = split;
		this.dividend = dividend;
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

	public DataHelper.CurrencyKey getCurrencyKey() {
		return currencyKey;
	}

	public void setCurrencyKey(DataHelper.CurrencyKey currencyKey) {
		this.currencyKey = currencyKey;
	}

	public BigDecimal getAdjClose() {
		return adjClose;
	}

	public void setAdjClose(BigDecimal adjClose) {
		this.adjClose = adjClose;
	}

	public BigDecimal getSplit() {
		return split;
	}

	public void setSplit(BigDecimal split) {
		this.split = split;
	}

	public BigDecimal getDividend() {
		return dividend;
	}

	public void setDividend(BigDecimal dividend) {
		this.dividend = dividend;
	}

}
