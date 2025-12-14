/**
 *    Copyright 2018 Sven Loesekann
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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyFxWrapperImportDto {
	@JsonProperty("Meta Data")
	private DailyFxMetaDataImportDto metadata;
	@JsonProperty("Time Series FX (Daily)")
	private Map<String,DailyFxQuoteImportDto> dailyQuotes = new HashMap<>();
	
	public DailyFxMetaDataImportDto getMetadata() {
		return metadata;
	}
	public void setMetadata(DailyFxMetaDataImportDto metadata) {
		this.metadata = metadata;
	}
	public Map<String, DailyFxQuoteImportDto> getDailyQuotes() {
		return dailyQuotes;
	}
	public void setDailyQuotes(Map<String, DailyFxQuoteImportDto> dailyQuotes) {
		this.dailyQuotes = dailyQuotes;
	}
}
