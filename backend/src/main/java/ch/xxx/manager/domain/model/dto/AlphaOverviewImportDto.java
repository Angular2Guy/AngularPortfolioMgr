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

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.xxx.manager.domain.utils.DataHelper;
import ch.xxx.manager.domain.utils.DataHelper.CurrencyKey;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaOverviewImportDto {
	@JsonProperty("Symbol")
	private String symbol;
	@JsonProperty("AssetType")
	private String assetType;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("Description")
	private String description;
	@JsonProperty("CIK")
	private String cik;
	@JsonProperty("Exchange")
	private String exchange;
	@Enumerated(EnumType.STRING)
	private DataHelper.CurrencyKey currency;
	@JsonProperty("Country")
	private String country;
	@JsonProperty("Sector")
	private String sector;
	@JsonProperty("Industry")
	private String industry;
	@JsonProperty("Address")
	private String address;
	@JsonProperty("SharesOutstanding")
	private Long sharesOutstanding;
	@JsonProperty("DividendDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dividendDate;
	@JsonProperty("ExDividendDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate exDividendDate;
	
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
	public LocalDate getDividendDate() {
		return dividendDate;
	}
	public void setDividendDate(LocalDate dividendDate) {
		this.dividendDate = dividendDate;
	}
	public String getCik() {
		return cik;
	}
	public void setCik(String cik) {
		this.cik = cik;
	}
	public Long getSharesOutstanding() {
		return sharesOutstanding;
	}
	public void setSharesOutstanding(Long sharesOutstanding) {
		this.sharesOutstanding = sharesOutstanding;
	}
	public LocalDate getExDividendDate() {
		return exDividendDate;
	}
	public void setExDividendDate(LocalDate exDividendDate) {
		this.exDividendDate = exDividendDate;
	}
	@Override
	public String toString() {
		return "AlphaOverviewImportDto [symbol=" + symbol + ", assetType=" + assetType + ", name=" + name
				+ ", description=" + description + ", cik=" + cik + ", exchange=" + exchange + ", currency=" + currency
				+ ", country=" + country + ", sector=" + sector + ", industry=" + industry + ", address=" + address
				+ ", sharesOutstanding=" + sharesOutstanding + ", dividendDate=" + dividendDate + ", exDividendDate="
				+ exDividendDate + "]";
	}
}
