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

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;

@Entity(name="portfolio")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="portfolio_type", 
  discriminatorType = DiscriminatorType.INTEGER)
public class PortfolioBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @SequenceGenerator(name="seq", sequenceName="hibernate_sequence")
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
	private Double year1BetaSp500;
	private Double year1BetaMsciChina;
	private Double year1BetaEuroStoxx50;
	private Double year2CorrelationSp500;
	private Double year2CorrelationMsciChina;
	private Double year2CorrelationEuroStoxx50;
	private Double year2BetaSp500;
	private Double year2BetaMsciChina;
	private Double year2BetaEuroStoxx50;
	private Double year5CorrelationSp500;
	private Double year5CorrelationMsciChina;
	private Double year5CorrelationEuroStoxx50;
	private Double year5BetaSp500;
	private Double year5BetaMsciChina;
	private Double year5BetaEuroStoxx50;
	private Double year10CorrelationSp500;
	private Double year10CorrelationMsciChina;
	private Double year10CorrelationEuroStoxx50;
	private Double year10BetaSp500;
	private Double year10BetaMsciChina;
	private Double year10BetaEuroStoxx50;
	
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

	public Double getYear1BetaSp500() {
		return year1BetaSp500;
	}

	public void setYear1BetaSp500(Double year1BetaSp500) {
		this.year1BetaSp500 = year1BetaSp500;
	}

	public Double getYear1BetaMsciChina() {
		return year1BetaMsciChina;
	}

	public void setYear1BetaMsciChina(Double year1BetaMsciChina) {
		this.year1BetaMsciChina = year1BetaMsciChina;
	}

	public Double getYear1BetaEuroStoxx50() {
		return year1BetaEuroStoxx50;
	}

	public void setYear1BetaEuroStoxx50(Double year1BetaEuroStoxx50) {
		this.year1BetaEuroStoxx50 = year1BetaEuroStoxx50;
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

	public Double getYear2BetaSp500() {
		return year2BetaSp500;
	}

	public void setYear2BetaSp500(Double year2BetaSp500) {
		this.year2BetaSp500 = year2BetaSp500;
	}

	public Double getYear2BetaMsciChina() {
		return year2BetaMsciChina;
	}

	public void setYear2BetaMsciChina(Double year2BetaMsciChina) {
		this.year2BetaMsciChina = year2BetaMsciChina;
	}

	public Double getYear2BetaEuroStoxx50() {
		return year2BetaEuroStoxx50;
	}

	public void setYear2BetaEuroStoxx50(Double year2BetaEuroStoxx50) {
		this.year2BetaEuroStoxx50 = year2BetaEuroStoxx50;
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

	public Double getYear5BetaSp500() {
		return year5BetaSp500;
	}

	public void setYear5BetaSp500(Double year5BetaSp500) {
		this.year5BetaSp500 = year5BetaSp500;
	}

	public Double getYear5BetaMsciChina() {
		return year5BetaMsciChina;
	}

	public void setYear5BetaMsciChina(Double year5BetaMsciChina) {
		this.year5BetaMsciChina = year5BetaMsciChina;
	}

	public Double getYear5BetaEuroStoxx50() {
		return year5BetaEuroStoxx50;
	}

	public void setYear5BetaEuroStoxx50(Double year5BetaEuroStoxx50) {
		this.year5BetaEuroStoxx50 = year5BetaEuroStoxx50;
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

	public Double getYear10BetaSp500() {
		return year10BetaSp500;
	}

	public void setYear10BetaSp500(Double year10BetaSp500) {
		this.year10BetaSp500 = year10BetaSp500;
	}

	public Double getYear10BetaMsciChina() {
		return year10BetaMsciChina;
	}

	public void setYear10BetaMsciChina(Double year10BetaMsciChina) {
		this.year10BetaMsciChina = year10BetaMsciChina;
	}

	public Double getYear10BetaEuroStoxx50() {
		return year10BetaEuroStoxx50;
	}

	public void setYear10BetaEuroStoxx50(Double year10BetaEuroStoxx50) {
		this.year10BetaEuroStoxx50 = year10BetaEuroStoxx50;
	}

}
