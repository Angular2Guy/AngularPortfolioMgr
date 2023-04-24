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
package ch.xxx.manager.domain.model.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

//@JsonInclude(value = Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown = true) 
public class FinancialElementImportDto {
	private Long id;
	private String label;
	private String concept;
	@JsonProperty(value = "unit")
	private String currency;
	private String value;
	private String info;
	private SymbolFinancialsDto symbolFinancialsDto;
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getConcept() {
		return concept;
	}
	public void setConcept(String concept) {
		this.concept = concept;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public SymbolFinancialsDto getSymbolFinancialsDto() {
		return symbolFinancialsDto;
	}
	public void setSymbolFinancialsDto(SymbolFinancialsDto symbolFinancialsDto) {
		this.symbolFinancialsDto = symbolFinancialsDto;
	}
	@Override
	public String toString() {
		return "FinancialElementDto [id=" + id + ", label=" + label + ", concept=" + concept + ", currency=" + currency
				+ ", value=" + value + ", symbolFinancialsDto=" + symbolFinancialsDto + "]";
	}
}
