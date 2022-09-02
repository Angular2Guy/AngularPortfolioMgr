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

import java.time.LocalDateTime;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.xxx.manager.domain.utils.CurrencyKey;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaOverviewImportDto {
	private String symbol;
	private String assetType;
	private String name;
	private String description;
	private String cIK;
	private String exchange;
	@Enumerated(EnumType.STRING)
	private CurrencyKey currency;
	private String country;
	private String sector;
	private String industry;
	private String address;
	private Long shareOutstanding;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDateTime dividendDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDateTime exDividentDate;
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getAssetType() {
		return assetType;
	}
	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getcIK() {
		return cIK;
	}
	public void setcIK(String cIK) {
		this.cIK = cIK;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public CurrencyKey getCurrency() {
		return currency;
	}
	public void setCurrency(CurrencyKey currency) {
		this.currency = currency;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getSector() {
		return sector;
	}
	public void setSector(String sector) {
		this.sector = sector;
	}
	public String getIndustry() {
		return industry;
	}
	public void setIndustry(String industry) {
		this.industry = industry;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Long getShareOutstanding() {
		return shareOutstanding;
	}
	public void setShareOutstanding(Long shareOutstanding) {
		this.shareOutstanding = shareOutstanding;
	}
	public LocalDateTime getDividendDate() {
		return dividendDate;
	}
	public void setDividendDate(LocalDateTime dividendDate) {
		this.dividendDate = dividendDate;
	}
	public LocalDateTime getExDividentDate() {
		return exDividentDate;
	}
	public void setExDividentDate(LocalDateTime exDividentDate) {
		this.exDividentDate = exDividentDate;
	}
	
	@Override
	public String toString() {
		return "AlphaOverviewImportDto [symbol=" + symbol + ", assetType=" + assetType + ", name=" + name
				+ ", description=" + description + ", cIK=" + cIK + ", exchange=" + exchange + ", currency=" + currency
				+ ", country=" + country + ", sector=" + sector + ", industry=" + industry + ", address=" + address
				+ ", shareOutstanding=" + shareOutstanding + ", dividendDate=" + dividendDate + ", exDividentDate="
				+ exDividentDate + "]";
	}
}
