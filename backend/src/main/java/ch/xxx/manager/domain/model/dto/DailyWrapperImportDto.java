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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyWrapperImportDto {
	@JsonProperty("Meta Data")
	private DailyMetaDataImportDto metaData;	
//	@JsonProperty("Time Series (Daily)")
//	private Map<String,DailyQuoteImportAdjDto> dailyQuotes = new HashMap<>();
	@JsonProperty("Time Series (Daily)")
	private Map<String,DailyQuoteImportDto> dailyQuotes = new HashMap<>();
	
	public DailyMetaDataImportDto getMetaData() {
		return metaData;
	}
	public void setMetaData(DailyMetaDataImportDto metaData) {
		this.metaData = metaData;
	}
//	public Map<String, DailyQuoteImportAdjDto> getDailyQuotes() {
//		return dailyQuotes;
//	}
//	public void setDailyQuotes(Map<String, DailyQuoteImportAdjDto> dailyQuotes) {
//		this.dailyQuotes = dailyQuotes;
//	}
	public Map<String, DailyQuoteImportDto> getDailyQuotes() {
		return dailyQuotes;
	}
	public void setDailyQuotes(Map<String, DailyQuoteImportDto> dailyQuotes) {
		this.dailyQuotes = dailyQuotes;
	}
}
