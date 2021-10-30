package ch.xxx.manager.domain.model.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PortfolioBarsDto {
	private String title;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDate from;
	private List<BarDto> barDtos = new ArrayList<>();
	
	public PortfolioBarsDto() {		
	}
	
	public PortfolioBarsDto(String title, LocalDate from, List<BarDto> barDtos) {
		this.title = title;				
		this.from = from;
		this.barDtos.addAll(barDtos);
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public LocalDate getFrom() {
		return from;
	}
	public void setFrom(LocalDate from) {
		this.from = from;
	}
	public List<BarDto> getBarDtos() {
		return barDtos;
	}
	public void setBarDtos(List<BarDto> barDtos) {
		this.barDtos = barDtos;
	}
}
