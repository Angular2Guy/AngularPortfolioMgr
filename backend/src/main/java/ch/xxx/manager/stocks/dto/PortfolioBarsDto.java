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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PortfolioBarsDto {
	private String title;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime from;
	private List<PortfolioBarDto> portfolioBars = new ArrayList<>();
	
	public PortfolioBarsDto() {		
	}
	
	public PortfolioBarsDto(String title, LocalDate from, List<PortfolioBarDto> portfolioBarDtos) {
		this.title = title;				
		this.from = from.atStartOfDay();
		this.portfolioBars.addAll(portfolioBarDtos);
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public LocalDateTime getFrom() {
		return from;
	}
	public void setFrom(LocalDateTime from) {
		this.from = from;
	}

	public List<PortfolioBarDto> getPortfolioBars() {
		return portfolioBars;
	}

	public void setPortfolioBars(List<PortfolioBarDto> portfolioBarDtos) {
		this.portfolioBars = portfolioBarDtos;
	}	
}
