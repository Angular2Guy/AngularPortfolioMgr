package ch.xxx.manager.usecase.mapping;

import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.PortfolioDto;
import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.utils.CurrencyKey;

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
		dto.setCurrencyKey(portfolio.getCurrencyKey());
		dto.getSymbols()
				.addAll(Optional.ofNullable(portfolio.getPortfolioToSymbols()).orElseGet(() -> Set.of()).stream()
						.flatMap(pts -> Stream.of(this.symbolMapper.convert(pts.getSymbol(), pts)))
						.collect(Collectors.toList()));
		return dto;
	}
	
	public Portfolio toEntity(PortfolioDto dto, AppUser appUser) {
		Portfolio entity = new Portfolio();
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setAppUser(Optional.ofNullable(appUser).orElse(null));
		entity.setCurrencyKey(Optional.ofNullable(dto.getCurrencyKey()).orElse(CurrencyKey.EUR));
		return entity;
	}
}
