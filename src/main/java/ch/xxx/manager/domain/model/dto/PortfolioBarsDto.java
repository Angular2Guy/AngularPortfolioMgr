package ch.xxx.manager.domain.model.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("portfolio-bars")
public class PortfolioBarsDto {
	private String title;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDate from;
	private List<PorfolioBarDto> portfolioBarDtos = new ArrayList<>();
	
	public PortfolioBarsDto() {		
	}
	
	public PortfolioBarsDto(String title, LocalDate from, List<PorfolioBarDto> portfolioBarDtos) {
		this.title = title;				
		this.from = from;
		this.portfolioBarDtos.addAll(portfolioBarDtos);
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

	public List<PorfolioBarDto> getPortfolioBarDtos() {
		return portfolioBarDtos;
	}

	public void setPortfolioBarDtos(List<PorfolioBarDto> portfolioBarDtos) {
		this.portfolioBarDtos = portfolioBarDtos;
	}	
}
