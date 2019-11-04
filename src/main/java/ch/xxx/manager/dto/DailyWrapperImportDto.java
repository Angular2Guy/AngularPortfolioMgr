package ch.xxx.manager.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyWrapperImportDto {
	@JsonProperty("Meta Data")
	private MetaDataImportDto metaData;	
	@JsonProperty("Time Series (Daily)")
	private Map<String,DailyQuoteImportDto> dailyQuotes = new HashMap<>();
	
	public MetaDataImportDto getMetaData() {
		return metaData;
	}
	public void setMetaData(MetaDataImportDto metaData) {
		this.metaData = metaData;
	}
	public Map<String, DailyQuoteImportDto> getDailyQuotes() {
		return dailyQuotes;
	}
	public void setDailyQuotes(Map<String, DailyQuoteImportDto> dailyQuotes) {
		this.dailyQuotes = dailyQuotes;
	}
}
