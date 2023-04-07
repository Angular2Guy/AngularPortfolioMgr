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
package ch.xxx.manager.usecase.mapping;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.PortfolioBarDto;
import ch.xxx.manager.domain.model.dto.PortfolioBarsDto;
import ch.xxx.manager.domain.model.dto.PortfolioDto;
import ch.xxx.manager.domain.model.dto.PortfolioElementDto;
import ch.xxx.manager.domain.model.dto.SymbolDto;
import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.domain.model.entity.Portfolio;
import ch.xxx.manager.domain.model.entity.PortfolioElement;
import ch.xxx.manager.domain.model.entity.dto.PortfolioBarsWrapper;
import ch.xxx.manager.domain.utils.DataHelper;

@Component
public class PortfolioMapper {
	private final SymbolMapper symbolMapper;

	public PortfolioMapper(SymbolMapper symbolMapper) {
		this.symbolMapper = symbolMapper;
	}

	public PortfolioDto toDtoFiltered(Portfolio portfolio) {
		PortfolioDto result = this.toDto(portfolio);
		@SuppressWarnings("unchecked")
		Map<String, SymbolDto>[] mapArr = new Map[1];
		mapArr[0] = new HashMap<String, SymbolDto>();
		result.getSymbols().stream().forEach(mySymbolDto -> {
			if (mapArr[0].get(mySymbolDto.getSymbol()) == null) {
				mapArr[0].put(mySymbolDto.getSymbol(), mySymbolDto);
			} else {
				SymbolDto mapSymbolDto = mapArr[0].get(mySymbolDto.getSymbol());
				if (mapSymbolDto.getChangedAt().isBefore(mySymbolDto.getChangedAt())) {
					mapArr[0].put(mySymbolDto.getSymbol(), mySymbolDto);
				}
			}
		});
		List<SymbolDto> symbolDtos = mapArr[0].entrySet().stream()
				.filter(myEntry -> myEntry.getValue().getRemovedAt() == null).map(myEntry -> myEntry.getValue())
				.toList();
		result.getSymbols().clear();
		result.getSymbols().addAll(symbolDtos);
		return result;
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
		List<PortfolioElementDto> portfolioElements = Optional.ofNullable(portfolio.getPortfolioElements())
				.orElse(Set.of()).stream()
				.flatMap(myPortfolioElement -> Stream.of(this.toPortfolioElementDto(myPortfolioElement))).toList();
		if (!portfolioElements.isEmpty()) {
			dto.getPortfolioElements().addAll(portfolioElements);
		}
		return dto;
	}

	public Portfolio toEntity(PortfolioDto dto, AppUser appUser) {
		Portfolio entity = new Portfolio();
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setAppUser(Optional.ofNullable(appUser).orElse(null));
		entity.setCurrencyKey(Optional.ofNullable(dto.getCurrencyKey()).orElse(DataHelper.CurrencyKey.EUR));
		return entity;
	}

	public PortfolioElementDto toPortfolioElementDto(PortfolioElement portfolioElement) {
		return new PortfolioElementDto(portfolioElement.getId(), portfolioElement.getName(),
				portfolioElement.getSymbol(), portfolioElement.getCurrencyKey(),
				portfolioElement.getCreatedAt().atStartOfDay(), portfolioElement.getLastClose(),
				portfolioElement.getMonth1(), portfolioElement.getMonth6(), portfolioElement.getYear1(),
				portfolioElement.getYear2(), portfolioElement.getYear5(), portfolioElement.getYear10(),
				portfolioElement.getWeight(), portfolioElement.getSector());
	}

	public PortfolioBarsDto toBarsDto(PortfolioBarsWrapper portfolioBarsWrapper) {
		List<PortfolioBarDto> portfolioBars = portfolioBarsWrapper.portfolioElements().stream()
				.map(pe -> new PortfolioBarDto(pe.value(), pe.symbolName(), BigDecimal.valueOf(pe.weight()))).toList();
		return new PortfolioBarsDto(portfolioBarsWrapper.portfolio().getName(), portfolioBarsWrapper.start(),
				portfolioBars);
	}

}