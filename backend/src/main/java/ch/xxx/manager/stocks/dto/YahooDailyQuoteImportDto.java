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

import java.math.BigDecimal;
import java.time.LocalDate;

public class YahooDailyQuoteImportDto {
//	@JsonProperty("Date")
//	@JsonFormat
//    (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate date;
//	@JsonProperty("Open")
	private BigDecimal open;
//	@JsonProperty("High")
	private BigDecimal high;
//	@JsonProperty("Low")
	private BigDecimal low;
//	@JsonProperty("Close")
	private BigDecimal close;
//	@JsonProperty("Adj Close")
	private BigDecimal adjClose;
//	@JsonProperty("Volume")
	private Long volume;
	private BigDecimal split;
	private BigDecimal dividend;
	
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
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
	public BigDecimal getAdjClose() {
		return adjClose;
	}
	public void setAdjClose(BigDecimal adjClose) {
		this.adjClose = adjClose;
	}
	public Long getVolume() {
		return volume;
	}
	public void setVolume(Long volume) {
		this.volume = volume;
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

	@Override
	public String toString() {
		return "YahooDailyQuoteImportDto [date=" + date + ", open=" + open + ", high=" + high + ", low=" + low
				+ ", close=" + close + ", adjClose=" + adjClose + ", volume=" + volume + ", split=" + split
				+ ", dividend=" + dividend + "]";
	}
}
