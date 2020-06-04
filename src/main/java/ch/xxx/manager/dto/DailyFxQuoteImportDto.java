package ch.xxx.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyFxQuoteImportDto {
	@JsonProperty("1. open")
	private String open;
	@JsonProperty("2. high")
	private String high;
	@JsonProperty("3. low")
	private String low;
	@JsonProperty("4. close")	
	private String close;
}
