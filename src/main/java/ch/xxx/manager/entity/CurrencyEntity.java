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

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("currency")
public class CurrencyEntity {
	@Id
	private Long id;
	private LocalDate localDay;
	private String from_curr;
	private String to_curr;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	
	public CurrencyEntity() {
	}
	
	public CurrencyEntity(LocalDate localDay, String from_curr, String to_curr, BigDecimal open, BigDecimal high, BigDecimal low,
			BigDecimal close) {
		super();
		this.localDay = localDay;
		this.from_curr = from_curr;
		this.to_curr = to_curr;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFrom_curr() {
		return from_curr;
	}

	public void setFrom_curr(String from_curr) {
		this.from_curr = from_curr;
	}

	public String getTo_curr() {
		return to_curr;
	}

	public void setTo_curr(String to_curr) {
		this.to_curr = to_curr;
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
}
