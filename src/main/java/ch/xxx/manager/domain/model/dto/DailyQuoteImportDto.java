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
package ch.xxx.manager.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyQuoteImportDto {	
	@JsonProperty("1. open")
	private String open;
	@JsonProperty("2. high")
	private String high;
	@JsonProperty("3. low")
	private String low;
	@JsonProperty("4. close")	
	private String close;
	@JsonProperty("6. volume")
	private String volume;
	@JsonProperty("5. adjusted close")
	private String ajustedClose;
	@JsonProperty("7. dividend amount")
	private String dividendAmount;
	@JsonProperty("8. split coefficient")
	private String splitCoeficent;
	
	public DailyQuoteImportDto() {		
	}
	
	public DailyQuoteImportDto(String open, String high, String low, String close, String volume, String ajustedClose,
			String dividendAmount, String splitCoeficent) {
		super();
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.ajustedClose = ajustedClose;
		this.dividendAmount = dividendAmount;
		this.splitCoeficent = splitCoeficent;
	}
	
	public String getOpen() {
		return open;
	}
	public void setOpen(String open) {
		this.open = open;
	}
	public String getHigh() {
		return high;
	}
	public void setHigh(String high) {
		this.high = high;
	}
	public String getLow() {
		return low;
	}
	public void setLow(String low) {
		this.low = low;
	}
	public String getClose() {
		return close;
	}
	public void setClose(String close) {
		this.close = close;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getAjustedClose() {
		return ajustedClose;
	}
	public void setAjustedClose(String ajustedClose) {
		this.ajustedClose = ajustedClose;
	}
	public String getDividendAmount() {
		return dividendAmount;
	}
	public void setDividendAmount(String dividendAmount) {
		this.dividendAmount = dividendAmount;
	}
	public String getSplitCoeficent() {
		return splitCoeficent;
	}
	public void setSplitCoeficent(String splitCoeficent) {
		this.splitCoeficent = splitCoeficent;
	}
}