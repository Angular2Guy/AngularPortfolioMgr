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

import jakarta.persistence.*;

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
    @Column(name="year1correlation_sp500")
	private Double year1CorrelationSp500;
    @Column(name="year1correlation_msci_china")
	private Double year1CorrelationMsciChina;
    @Column(name="year1correlation_euro_stoxx50")
	private Double year1CorrelationEuroStoxx50;
    @Column(name="year1lin_reg_return_sp500")
	private Double year1LinRegReturnSp500;
    @Column(name="year1lin_reg_return_msci_china")
	private Double year1LinRegReturnMsciChina;
    @Column(name="year1lin_reg_return_euro_stoxx50")
	private Double year1LinRegReturnEuroStoxx50;
    @Column(name="year1sigma_sp500")
	private Double year1SigmaSp500;
    @Column(name="year1sigma_msci_china")
	private Double year1SigmaMsciChina;
    @Column(name="year1sigma_euro_stoxx50")
	private Double year1SigmaEuroStoxx50;
    @Column(name="year1sigma_portfolio")
	private Double year1SigmaPortfolio;
    @Column(name="year2correlation_sp500")
	private Double year2CorrelationSp500;
    @Column(name="year2correlation_msci_china")
	private Double year2CorrelationMsciChina;
    @Column(name="year2correlation_euro_stoxx50")
	private Double year2CorrelationEuroStoxx50;
    @Column(name="year2lin_reg_return_sp500")
	private Double year2LinRegReturnSp500;
    @Column(name="year2lin_reg_return_msci_china")
	private Double year2LinRegReturnMsciChina;
    @Column(name="year2lin_reg_return_euro_stoxx50")
	private Double year2LinRegReturnEuroStoxx50;
    @Column(name="year2sigma_sp500")
	private Double year2SigmaSp500;
    @Column(name="year2sigma_msci_china")
	private Double year2SigmaMsciChina;
    @Column(name="year2sigma_euro_stoxx50")
	private Double year2SigmaEuroStoxx50;
    @Column(name="year2sigma_portfolio")
	private Double year2SigmaPortfolio;
    @Column(name="year5correlation_sp500")
	private Double year5CorrelationSp500;
    @Column(name="year5correlation_msci_china")
	private Double year5CorrelationMsciChina;
    @Column(name="year5correlation_euro_stoxx50")
	private Double year5CorrelationEuroStoxx50;
    @Column(name="year5lin_reg_return_sp500")
	private Double year5LinRegReturnSp500;
    @Column(name="year5lin_reg_return_msci_china")
	private Double year5LinRegReturnMsciChina;
    @Column(name="year5lin_reg_return_euro_stoxx50")
	private Double year5LinRegReturnEuroStoxx50;
    @Column(name="year5sigma_sp500")
	private Double year5SigmaSp500;
    @Column(name="year5sigma_msci_china")
	private Double year5SigmaMsciChina;
    @Column(name="year5sigma_euro_stoxx50")
	private Double year5SigmaEuroStoxx50;
    @Column(name="year5sigma_portfolio")
	private Double year5SigmaPortfolio;
    @Column(name="year10correlation_sp500")
	private Double year10CorrelationSp500;
    @Column(name="year10correlation_msci_china")
	private Double year10CorrelationMsciChina;
    @Column(name = "year10correlation_euro_stoxx50")
	private Double year10CorrelationEuroStoxx50;
    @Column(name="year10lin_reg_return_sp500")
	private Double year10LinRegReturnSp500;
    @Column(name="year10lin_reg_return_msci_china")
	private Double year10LinRegReturnMsciChina;
    @Column(name="year10lin_reg_return_euro_stoxx50")
	private Double year10LinRegReturnEuroStoxx50;
    @Column(name="year10sigma_sp500")
	private Double year10SigmaSp500;
    @Column(name="year10sigma_msci_china")
	private Double year10SigmaMsciChina;
    @Column(name="year10sigma_euro_stoxx50")
	private Double year10SigmaEuroStoxx50;
    @Column(name="year10sigma_portfolio")
	private Double year10SigmaPortfolio;

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

	public Double getYear1SigmaSp500() {
		return year1SigmaSp500;
	}

	public void setYear1SigmaSp500(Double year1SigmaSp500) {
		this.year1SigmaSp500 = year1SigmaSp500;
	}

	public Double getYear1SigmaMsciChina() {
		return year1SigmaMsciChina;
	}

	public void setYear1SigmaMsciChina(Double year1SigmaMsciChina) {
		this.year1SigmaMsciChina = year1SigmaMsciChina;
	}

	public Double getYear2SigmaSp500() {
		return year2SigmaSp500;
	}

	public void setYear2SigmaSp500(Double year2SigmaSp500) {
		this.year2SigmaSp500 = year2SigmaSp500;
	}

	public Double getYear2SigmaMsciChina() {
		return year2SigmaMsciChina;
	}

	public void setYear2SigmaMsciChina(Double year2SigmaMsciChina) {
		this.year2SigmaMsciChina = year2SigmaMsciChina;
	}

	public Double getYear5SigmaSp500() {
		return year5SigmaSp500;
	}

	public void setYear5SigmaSp500(Double year5SigmaSp500) {
		this.year5SigmaSp500 = year5SigmaSp500;
	}

	public Double getYear5SigmaMsciChina() {
		return year5SigmaMsciChina;
	}

	public void setYear5SigmaMsciChina(Double year5SigmaMsciChina) {
		this.year5SigmaMsciChina = year5SigmaMsciChina;
	}

	public Double getYear10SigmaSp500() {
		return year10SigmaSp500;
	}

	public void setYear10SigmaSp500(Double year10SigmaSp500) {
		this.year10SigmaSp500 = year10SigmaSp500;
	}

	public Double getYear10SigmaMsciChina() {
		return year10SigmaMsciChina;
	}

	public void setYear10SigmaMsciChina(Double year10SigmaMsciChina) {
		this.year10SigmaMsciChina = year10SigmaMsciChina;
	}

	public Double getYear1SigmaEuroStoxx50() {
		return year1SigmaEuroStoxx50;
	}

	public void setYear1SigmaEuroStoxx50(Double year1SigmaEuroStoxx50) {
		this.year1SigmaEuroStoxx50 = year1SigmaEuroStoxx50;
	}

	public Double getYear2SigmaEuroStoxx50() {
		return year2SigmaEuroStoxx50;
	}

	public void setYear2SigmaEuroStoxx50(Double year2SigmaEuroStoxx50) {
		this.year2SigmaEuroStoxx50 = year2SigmaEuroStoxx50;
	}

	public Double getYear5SigmaEuroStoxx50() {
		return year5SigmaEuroStoxx50;
	}

	public void setYear5SigmaEuroStoxx50(Double year5SigmaEuroStoxx50) {
		this.year5SigmaEuroStoxx50 = year5SigmaEuroStoxx50;
	}

	public Double getYear10SigmaEuroStoxx50() {
		return year10SigmaEuroStoxx50;
	}

	public void setYear10SigmaEuroStoxx50(Double year10SigmaEuroStoxx50) {
		this.year10SigmaEuroStoxx50 = year10SigmaEuroStoxx50;
	}

	public Double getYear1SigmaPortfolio() {
		return year1SigmaPortfolio;
	}

	public void setYear1SigmaPortfolio(Double year1SigmaPortfolio) {
		this.year1SigmaPortfolio = year1SigmaPortfolio;
	}

	public Double getYear2SigmaPortfolio() {
		return year2SigmaPortfolio;
	}

	public void setYear2SigmaPortfolio(Double year2SigmaPortfolio) {
		this.year2SigmaPortfolio = year2SigmaPortfolio;
	}

	public Double getYear5SigmaPortfolio() {
		return year5SigmaPortfolio;
	}

	public void setYear5SigmaPortfolio(Double year5SigmaPortfolio) {
		this.year5SigmaPortfolio = year5SigmaPortfolio;
	}

	public Double getYear10SigmaPortfolio() {
		return year10SigmaPortfolio;
	}

	public void setYear10SigmaPortfolio(Double year10SigmaPortfolio) {
		this.year10SigmaPortfolio = year10SigmaPortfolio;
	}
}
