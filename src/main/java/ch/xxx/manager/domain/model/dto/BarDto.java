package ch.xxx.manager.domain.model.dto;

import java.math.BigDecimal;

public class BarDto {
	private BigDecimal value;
	private String name;
	private BigDecimal weight;
	
	public BarDto() {		
	}
	
	public BarDto(BigDecimal value, String name, BigDecimal weight) {
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
