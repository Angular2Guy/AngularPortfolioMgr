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
package ch.xxx.manager.findata.entity;

import java.math.BigDecimal;

import ch.xxx.manager.common.entity.EntityBase;
import ch.xxx.manager.common.utils.DataHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class FinancialElement extends EntityBase {
	@Lob
	@Column(columnDefinition = "text")
	private String label;
	private String concept;
	@NotNull
	@Enumerated(EnumType.STRING)
	private DataHelper.FinancialElementType financialElementType;
	@NotNull
	@Enumerated(EnumType.STRING)
	private DataHelper.CurrencyKey currency;
	@Column(name="`value`")
	private BigDecimal value;	
	private String info;
	@ManyToOne
	private SymbolFinancials symbolFinancials;
	
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
	public DataHelper.CurrencyKey getCurrency() {
		return currency;
	}
	public void setCurrency(DataHelper.CurrencyKey currency) {
		this.currency = currency;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public SymbolFinancials getSymbolFinancials() {
		return symbolFinancials;
	}
	public void setSymbolFinancials(SymbolFinancials symbolFinancials) {
		this.symbolFinancials = symbolFinancials;
	}
	public DataHelper.FinancialElementType getFinancialElementType() {
		return financialElementType;
	}
	public void setFinancialElementType(DataHelper.FinancialElementType financialElementType) {
		this.financialElementType = financialElementType;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
}
