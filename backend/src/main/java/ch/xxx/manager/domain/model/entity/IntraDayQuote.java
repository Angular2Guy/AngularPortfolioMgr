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
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ch.xxx.manager.domain.utils.CurrencyKey;

@Entity
public class IntraDayQuote extends EntityBase {
	@NotBlank
	@Size(max = 20)
	private String symbolKey;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	@NotNull
	private BigDecimal close;
	private Long volume;
	private LocalDateTime localDateTime;
	@ManyToOne(fetch = FetchType.LAZY)
	private Symbol symbol;
	@NotNull
	@Enumerated(EnumType.STRING)
	private CurrencyKey currencyKey;

	public IntraDayQuote() {
		super();
	}

	public IntraDayQuote(Long id, String symbolKey, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
			Long volume, LocalDateTime localDateTime, Symbol symbol, CurrencyKey currencyKey) {
		super();
		super.setId(id);
		this.symbolKey = symbolKey;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.localDateTime = localDateTime;
		this.symbol = symbol;
		this.currencyKey = currencyKey;
	}

	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}

	public void setLocalDateTime(LocalDateTime localDateTime) {
		this.localDateTime = localDateTime;
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
}