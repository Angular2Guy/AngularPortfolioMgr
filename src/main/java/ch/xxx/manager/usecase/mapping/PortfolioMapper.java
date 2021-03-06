package ch.xxx.manager.usecase.mapping;

import java.time.LocalTime;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.PortfolioDto;
import ch.xxx.manager.domain.model.entity.Portfolio;

@Component
public class PortfolioMapper {
	private final SymbolMapper symbolMapper;

	public PortfolioMapper(SymbolMapper symbolMapper) {
		this.symbolMapper = symbolMapper;
	}

	public PortfolioDto toDto(Portfolio portfolio) {
		PortfolioDto dto = new PortfolioDto();
		dto.setCreatedAt(portfolio.getCreatedAt().atTime(LocalTime.now()));
		dto.setId(portfolio.getId());
		dto.setMonth1(portfolio.getMonth1());
		dto.setMonth6(portfolio.getMonth6());
		dto.setName(portfolio.getName());
		dto.setUserId(portfolio.getAppUser().getId());
		dto.setYear1(portfolio.getYear1());
		dto.setYear2(portfolio.getYear2());
		dto.setYear5(portfolio.getYear5());
		dto.setYear10(portfolio.getYear10());
		dto.getSymbols().addAll(portfolio.getPortfolioToSymbols().stream()
				.map(pts -> this.symbolMapper.convert(pts.getSymbol())).collect(Collectors.toList()));
		return dto;
	}
}
