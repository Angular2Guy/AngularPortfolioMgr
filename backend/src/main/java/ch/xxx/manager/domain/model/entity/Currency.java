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

import ch.xxx.manager.domain.utils.CurrencyKey;

@Entity
public class Currency extends EntityBase {
	private LocalDate localDay;
	@Enumerated(EnumType.STRING)
	private CurrencyKey fromCurrKey;
	@Enumerated(EnumType.STRING)
	private CurrencyKey toCurrKey;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;

	public Currency() {
		super();
	}

	public Currency(LocalDate localDay, CurrencyKey fromCurrKey, CurrencyKey toCurrKey, BigDecimal open,
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

	public CurrencyKey getFromCurrKey() {
		return fromCurrKey;
	}

	public void setFromCurrKey(CurrencyKey fromCurrKey) {
		this.fromCurrKey = fromCurrKey;
	}

	public CurrencyKey getToCurrKey() {
		return toCurrKey;
	}

	public void setToCurrKey(CurrencyKey toCurrKey) {
		this.toCurrKey = toCurrKey;
	}

	@Override
	public String toString() {
		return "Currency [id=" + this.getId() + ", localDay=" + localDay + ", fromCurrKey=" + fromCurrKey + ", toCurrKey="
				+ toCurrKey + ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close + "]";
	}

}
