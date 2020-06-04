package ch.xxx.manager.dto;

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
