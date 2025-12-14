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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RapidOverviewImportDto {
	private String symbol;
	private RapidQuoteTypeDto quoteType = new RapidQuoteTypeDto();
	private RapidAssetProfileDto assetProfile = new RapidAssetProfileDto();
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public RapidQuoteTypeDto getQuoteType() {
		return quoteType;
	}
	public void setQuoteType(RapidQuoteTypeDto quoteType) {
		this.quoteType = quoteType;
	}	
	public RapidAssetProfileDto getAssetProfile() {
		return assetProfile;
	}
	public void setAssetProfile(RapidAssetProfileDto assetProfile) {
		this.assetProfile = assetProfile;
	}
	@Override
	public String toString() {
		return "RapidOverviewImportDto [symbol=" + symbol + ", quoteType=" + quoteType + ", assetProfile="
				+ assetProfile + "]";
	}
}
