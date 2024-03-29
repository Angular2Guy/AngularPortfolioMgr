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
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import ch.xxx.manager.domain.utils.DataHelper;


public class SymbolDto {
	private Long id;
	private String symbol;
	private String name;
	private Long weight;
	private String source;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime changedAt;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime removedAt;
	private DataHelper.CurrencyKey currencyKey;
	private String sector;
	private String industry;
	private String description;
	private String address;
	private String country;

	public SymbolDto() {

	}

	public SymbolDto(Long id, String symbol, String name, LocalDate changedAt, LocalDate removedAt, String source,
			Long weight, DataHelper.CurrencyKey currencyKey, String sector, String industry, String description, String address,
			String country) {
		super();
		this.id = id;
		this.symbol = symbol;
		this.name = name;
		this.source = source;
		this.weight = weight;
		this.changedAt = changedAt == null ? null : LocalDateTime.of(changedAt, LocalTime.now());
		this.removedAt = removedAt == null ? null : LocalDateTime.of(removedAt, LocalTime.now());
		this.currencyKey = currencyKey;
		this.sector = sector;
		this.industry = industry;
		this.description = description;
		this.address = address;
		this.country = country;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(LocalDateTime changedAt) {
		this.changedAt = changedAt;
	}

	public LocalDateTime getRemovedAt() {
		return removedAt;
	}

	public void setRemovedAt(LocalDateTime removedAt) {
		this.removedAt = removedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public DataHelper.CurrencyKey getCurrencyKey() {
		return currencyKey;
	}

	public void setCurrencyKey(DataHelper.CurrencyKey currencyKey) {
		this.currencyKey = currencyKey;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

}
