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
package ch.xxx.manager.domain.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SequenceGenerator;

@Entity(name = "portfolio")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "portfolio_type", discriminatorType = DiscriminatorType.INTEGER)
public class PortfolioBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
	@SequenceGenerator(name = "seq", sequenceName = "hibernate_sequence", allocationSize = 50)
	private Long id;
	private BigDecimal month1;
	private BigDecimal month6;
	private BigDecimal year1;
	private BigDecimal year2;
	private BigDecimal year5;
	private BigDecimal year10;
	private Double year1CorrelationSp500;
	private Double year1CorrelationMsciChina;
	private Double year1CorrelationEuroStoxx50;
	private Double year1LinRegReturnSp500;
	private Double year1LinRegReturnMsciChina;
	private Double year1LinRegReturnEuroStoxx50;
	private Double year1Sigma;
	private Double year2CorrelationSp500;
	private Double year2CorrelationMsciChina;
	private Double year2CorrelationEuroStoxx50;
	private Double year2LinRegReturnSp500;
	private Double year2LinRegReturnMsciChina;
	private Double year2LinRegReturnEuroStoxx50;
	private Double year2Sigma;
	private Double year5CorrelationSp500;
	private Double year5CorrelationMsciChina;
	private Double year5CorrelationEuroStoxx50;
	private Double year5LinRegReturnSp500;
	private Double year5LinRegReturnMsciChina;
	private Double year5LinRegReturnEuroStoxx50;
	private Double year5Sigma;
	private Double year10CorrelationSp500;
	private Double year10CorrelationMsciChina;
	private Double year10CorrelationEuroStoxx50;
	private Double year10LinRegReturnSp500;
	private Double year10LinRegReturnMsciChina;
	private Double year10LinRegReturnEuroStoxx50;
	private Double year10Sigma;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getMonth1() {
		return month1;
	}

	public void setMonth1(BigDecimal month1) {
		this.month1 = month1;
	}

	public BigDecimal getMonth6() {
		return month6;
	}

	public void setMonth6(BigDecimal month6) {
		this.month6 = month6;
	}

	public BigDecimal getYear1() {
		return year1;
	}

	public void setYear1(BigDecimal year1) {
		this.year1 = year1;
	}

	public BigDecimal getYear2() {
		return year2;
	}

	public void setYear2(BigDecimal year2) {
		this.year2 = year2;
	}

	public BigDecimal getYear5() {
		return year5;
	}

	public void setYear5(BigDecimal year5) {
		this.year5 = year5;
	}

	public BigDecimal getYear10() {
		return year10;
	}

	public void setYear10(BigDecimal year10) {
		this.year10 = year10;
	}

	public Double getYear1CorrelationSp500() {
		return year1CorrelationSp500;
	}

	public void setYear1CorrelationSp500(Double year1CorrelationSp500) {
		this.year1CorrelationSp500 = year1CorrelationSp500;
	}

	public Double getYear1CorrelationMsciChina() {
		return year1CorrelationMsciChina;
	}

	public void setYear1CorrelationMsciChina(Double year1CorrelationMsciChina) {
		this.year1CorrelationMsciChina = year1CorrelationMsciChina;
	}

	public Double getYear1CorrelationEuroStoxx50() {
		return year1CorrelationEuroStoxx50;
	}

	public void setYear1CorrelationEuroStoxx50(Double year1CorrelationEuroStoxx50) {
		this.year1CorrelationEuroStoxx50 = year1CorrelationEuroStoxx50;
	}

	public Double getYear2CorrelationSp500() {
		return year2CorrelationSp500;
	}

	public void setYear2CorrelationSp500(Double year2CorrelationSp500) {
		this.year2CorrelationSp500 = year2CorrelationSp500;
	}

	public Double getYear2CorrelationMsciChina() {
		return year2CorrelationMsciChina;
	}

	public void setYear2CorrelationMsciChina(Double year2CorrelationMsciChina) {
		this.year2CorrelationMsciChina = year2CorrelationMsciChina;
	}

	public Double getYear2CorrelationEuroStoxx50() {
		return year2CorrelationEuroStoxx50;
	}

	public void setYear2CorrelationEuroStoxx50(Double year2CorrelationEuroStoxx50) {
		this.year2CorrelationEuroStoxx50 = year2CorrelationEuroStoxx50;
	}

	public Double getYear5CorrelationSp500() {
		return year5CorrelationSp500;
	}

	public void setYear5CorrelationSp500(Double year5CorrelationSp500) {
		this.year5CorrelationSp500 = year5CorrelationSp500;
	}

	public Double getYear5CorrelationMsciChina() {
		return year5CorrelationMsciChina;
	}

	public void setYear5CorrelationMsciChina(Double year5CorrelationMsciChina) {
		this.year5CorrelationMsciChina = year5CorrelationMsciChina;
	}

	public Double getYear5CorrelationEuroStoxx50() {
		return year5CorrelationEuroStoxx50;
	}

	public void setYear5CorrelationEuroStoxx50(Double year5CorrelationEuroStoxx50) {
		this.year5CorrelationEuroStoxx50 = year5CorrelationEuroStoxx50;
	}

	public Double getYear10CorrelationSp500() {
		return year10CorrelationSp500;
	}

	public void setYear10CorrelationSp500(Double year10CorrelationSp500) {
		this.year10CorrelationSp500 = year10CorrelationSp500;
	}

	public Double getYear10CorrelationMsciChina() {
		return year10CorrelationMsciChina;
	}

	public void setYear10CorrelationMsciChina(Double year10CorrelationMsciChina) {
		this.year10CorrelationMsciChina = year10CorrelationMsciChina;
	}

	public Double getYear10CorrelationEuroStoxx50() {
		return year10CorrelationEuroStoxx50;
	}

	public void setYear10CorrelationEuroStoxx50(Double year10CorrelationEuroStoxx50) {
		this.year10CorrelationEuroStoxx50 = year10CorrelationEuroStoxx50;
	}

	public Double getYear1LinRegReturnSp500() {
		return year1LinRegReturnSp500;
	}

	public void setYear1LinRegReturnSp500(Double year1LinRegReturnSp500) {
		this.year1LinRegReturnSp500 = year1LinRegReturnSp500;
	}

	public Double getYear1LinRegReturnMsciChina() {
		return year1LinRegReturnMsciChina;
	}

	public void setYear1LinRegReturnMsciChina(Double year1LinRegReturnMsciChina) {
		this.year1LinRegReturnMsciChina = year1LinRegReturnMsciChina;
	}

	public Double getYear1LinRegReturnEuroStoxx50() {
		return year1LinRegReturnEuroStoxx50;
	}

	public void setYear1LinRegReturnEuroStoxx50(Double year1LinRegReturnEuroStoxx50) {
		this.year1LinRegReturnEuroStoxx50 = year1LinRegReturnEuroStoxx50;
	}

	public Double getYear2LinRegReturnSp500() {
		return year2LinRegReturnSp500;
	}

	public void setYear2LinRegReturnSp500(Double year2LinRegReturnSp500) {
		this.year2LinRegReturnSp500 = year2LinRegReturnSp500;
	}

	public Double getYear2LinRegReturnMsciChina() {
		return year2LinRegReturnMsciChina;
	}

	public void setYear2LinRegReturnMsciChina(Double year2LinRegReturnMsciChina) {
		this.year2LinRegReturnMsciChina = year2LinRegReturnMsciChina;
	}

	public Double getYear2LinRegReturnEuroStoxx50() {
		return year2LinRegReturnEuroStoxx50;
	}

	public void setYear2LinRegReturnEuroStoxx50(Double year2LinRegReturnEuroStoxx50) {
		this.year2LinRegReturnEuroStoxx50 = year2LinRegReturnEuroStoxx50;
	}

	public Double getYear5LinRegReturnSp500() {
		return year5LinRegReturnSp500;
	}

	public void setYear5LinRegReturnSp500(Double year5LinRegReturnSp500) {
		this.year5LinRegReturnSp500 = year5LinRegReturnSp500;
	}

	public Double getYear5LinRegReturnMsciChina() {
		return year5LinRegReturnMsciChina;
	}

	public void setYear5LinRegReturnMsciChina(Double year5LinRegReturnMsciChina) {
		this.year5LinRegReturnMsciChina = year5LinRegReturnMsciChina;
	}

	public Double getYear5LinRegReturnEuroStoxx50() {
		return year5LinRegReturnEuroStoxx50;
	}

	public void setYear5LinRegReturnEuroStoxx50(Double year5LinRegReturnEuroStoxx50) {
		this.year5LinRegReturnEuroStoxx50 = year5LinRegReturnEuroStoxx50;
	}

	public Double getYear10LinRegReturnSp500() {
		return year10LinRegReturnSp500;
	}

	public void setYear10LinRegReturnSp500(Double year10LinRegReturnSp500) {
		this.year10LinRegReturnSp500 = year10LinRegReturnSp500;
	}

	public Double getYear10LinRegReturnMsciChina() {
		return year10LinRegReturnMsciChina;
	}

	public void setYear10LinRegReturnMsciChina(Double year10LinRegReturnMsciChina) {
		this.year10LinRegReturnMsciChina = year10LinRegReturnMsciChina;
	}

	public Double getYear10LinRegReturnEuroStoxx50() {
		return year10LinRegReturnEuroStoxx50;
	}

	public void setYear10LinRegReturnEuroStoxx50(Double year10LinRegReturnEuroStoxx50) {
		this.year10LinRegReturnEuroStoxx50 = year10LinRegReturnEuroStoxx50;
	}

	public Double getYear1Sigma() {
		return year1Sigma;
	}

	public void setYear1Sigma(Double year1Sigma) {
		this.year1Sigma = year1Sigma;
	}

	public Double getYear2Sigma() {
		return year2Sigma;
	}

	public void setYear2Sigma(Double year2Sigma) {
		this.year2Sigma = year2Sigma;
	}

	public Double getYear5Sigma() {
		return year5Sigma;
	}

	public void setYear5Sigma(Double year5Sigma) {
		this.year5Sigma = year5Sigma;
	}

	public Double getYear10Sigma() {
		return year10Sigma;
	}

	public void setYear10Sigma(Double year10Sigma) {
		this.year10Sigma = year10Sigma;
	}
}
