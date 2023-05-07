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

import java.math.BigDecimal;

import ch.xxx.manager.domain.utils.DataHelper.CurrencyKey;
import ch.xxx.manager.domain.utils.DataHelper.FinancialElementType;

public class FinancialElementDto {
	private Long id;
	private String label;
	private String concept;
	private FinancialElementType financialElementType;
	private CurrencyKey currency;
	private BigDecimal value;
	
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
	public FinancialElementType getFinancialElementType() {
		return financialElementType;
	}
	public void setFinancialElementType(FinancialElementType financialElementType) {
		this.financialElementType = financialElementType;
	}
	public CurrencyKey getCurrency() {
		return currency;
	}
	public void setCurrency(CurrencyKey currency) {
		this.currency = currency;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
