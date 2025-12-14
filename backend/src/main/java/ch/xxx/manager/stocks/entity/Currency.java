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
package ch.xxx.manager.stocks.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import ch.xxx.manager.common.entity.EntityBase;
import ch.xxx.manager.common.utils.DataHelper;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

@Entity
public class Currency extends EntityBase {
	@NotNull
	private LocalDate localDay;
	@NotNull
	@Enumerated(EnumType.STRING)
	private DataHelper.CurrencyKey fromCurrKey;
	@NotNull
	@Enumerated(EnumType.STRING)
	private DataHelper.CurrencyKey toCurrKey;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	@NotNull
	private BigDecimal close;

	public Currency() {
		super();
	}

	public Currency(LocalDate localDay, DataHelper.CurrencyKey fromCurrKey, DataHelper.CurrencyKey toCurrKey, BigDecimal open,
			BigDecimal high, BigDecimal low, BigDecimal close) {
		super();
		this.localDay = localDay;
		this.fromCurrKey = fromCurrKey;
		this.toCurrKey = toCurrKey;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
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

	public LocalDate getLocalDay() {
		return localDay;
	}

	public void setLocalDay(LocalDate localDay) {
		this.localDay = localDay;
	}

	public DataHelper.CurrencyKey getFromCurrKey() {
		return fromCurrKey;
	}

	public void setFromCurrKey(DataHelper.CurrencyKey fromCurrKey) {
		this.fromCurrKey = fromCurrKey;
	}

	public DataHelper.CurrencyKey getToCurrKey() {
		return toCurrKey;
	}

	public void setToCurrKey(DataHelper.CurrencyKey toCurrKey) {
		this.toCurrKey = toCurrKey;
	}
}
