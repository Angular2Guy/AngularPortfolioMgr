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
package ch.xxx.manager.domain.model.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyFxMetaDataImportDto {
	@JsonProperty("1. Information")
	private String info;
	@JsonProperty("2. From Symbol")
	private String fromSymbol;
	@JsonProperty("3. To Symbol")
	private String toSymbol;
	@JsonProperty("4. Output Size")
	private String outputSize;
	@JsonProperty("5. Last Refreshed")
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private String lastRefreshedStr;
	private LocalDate lastRefreshed;
	@JsonProperty("6. Time Zone")
	private String timeZone;

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
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

	public String getFromSymbol() {
		return fromSymbol;
	}

	public void setFromSymbol(String fromSymbol) {
		this.fromSymbol = fromSymbol;
	}

	public String getToSymbol() {
		return toSymbol;
	}

	public void setToSymbol(String toSymbol) {
		this.toSymbol = toSymbol;
	}

	public String getLastRefreshedStr() {
		return lastRefreshedStr;
	}

	public void setLastRefreshedStr(String lastRefreshedStr) {
		this.lastRefreshedStr = lastRefreshedStr;
		try {
			this.lastRefreshed = LocalDate.parse(lastRefreshedStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		} catch (DateTimeParseException e) {
			try {
				this.lastRefreshed = LocalDate.parse(lastRefreshedStr,
						DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			} catch (DateTimeParseException e1) {
				this.lastRefreshed = null;
			}
		}
	}
}
