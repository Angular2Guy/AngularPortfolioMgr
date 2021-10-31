package ch.xxx.manager.domain.model.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("portfolio-bar")
public class PorfolioBarDto {
	private BigDecimal value;
	private String name;
	private BigDecimal weight;
	
	public PorfolioBarDto() {		
	}
	
	public PorfolioBarDto(BigDecimal value, String name, BigDecimal weight) {
		this.value = value;
		this.name = name;
		this.weight = weight;
	}
	
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getWeight() {
		return weight;
	}
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
	
}
