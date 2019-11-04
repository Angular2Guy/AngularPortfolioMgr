package ch.xxx.manager.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaDataImportDto {
	@JsonProperty("1. Information")
	private String info;
	@JsonProperty("2. Symbol")
	private String symbol;
	@JsonProperty("3. Last Refreshed")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate lastRefreshed;
	@JsonProperty("4. Output Size")
	private String outputSize;
	@JsonProperty("5. Time Zone")
	private String timeZone;
	
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public LocalDate getLastRefreshed() {
		return lastRefreshed;
	}
	public void setLastRefreshed(LocalDate lastRefreshed) {
		this.lastRefreshed = lastRefreshed;
	}
	public String getOutputSize() {
		return outputSize;
	}
	public void setOutputSize(String outputSize) {
		this.outputSize = outputSize;
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}		
}
