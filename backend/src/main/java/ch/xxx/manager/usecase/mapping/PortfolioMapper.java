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

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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
import ch.xxx.manager.domain.utils.StreamHelpers;

@Component
public class PortfolioMapper {
	private final SymbolMapper symbolMapper;

	public PortfolioMapper(SymbolMapper symbolMapper) {
		this.symbolMapper = symbolMapper;
	}

	public PortfolioDto toDtoFiltered(Portfolio portfolio, List<PortfolioElement> portfolioElements) {
		PortfolioDto result = this.toDto(portfolio, portfolioElements);
		final var mapRef = new AtomicReference<Map<String, SymbolDto>>(new HashMap<String, SymbolDto>());
		result.getSymbols().stream().forEach(mySymbolDto -> {
			if (mapRef.get().get(mySymbolDto.getSymbol()) == null) {
				mapRef.get().put(mySymbolDto.getSymbol(), mySymbolDto);
			} else {
				SymbolDto mapSymbolDto = mapRef.get().get(mySymbolDto.getSymbol());
				if (mapSymbolDto.getChangedAt().isBefore(mySymbolDto.getChangedAt())) {
					mapRef.get().put(mySymbolDto.getSymbol(), mySymbolDto);
				}
			}
		});
		List<SymbolDto> symbolDtos = mapRef.get().entrySet().stream()
				.filter(myEntry -> myEntry.getValue().getRemovedAt() == null).map(myEntry -> myEntry.getValue())
				.toList();
		result.getSymbols().clear();
		result.getSymbols().addAll(symbolDtos);
		return result;
	}

	public PortfolioDto toDto(Portfolio portfolio, List<PortfolioElement> portfolioElements) {
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

		final var years = List.of("10", "5", "2", "1");

		final var methods = List.of("%sYear%sCorrelationEuroStoxx50", "%sYear%sCorrelationMsciChina",
				"%sYear%sCorrelationSp500", "%sYear%sLinRegReturnEuroStoxx50", "%sYear%sLinRegReturnMsciChina",
				"%sYear%sLinRegReturnSp500", "%sYear%sSigmaEuroStoxx50", "%sYear%sSigmaMsciChina", "%sYear%sSigmaSp500",
				"%sYear%sSigmaPortfolio");

		//not a Jvm hotspot
		years.forEach(myYear -> methods.forEach(myMethodStr -> this.setValue(String.format(myMethodStr, "set", myYear),
				Optional.ofNullable(this.getValue(String.format(myMethodStr, "get", myYear), Double.class, portfolio)), dto)));

		dto.setCurrencyKey(portfolio.getCurrencyKey());
		dto.getSymbols()
				.addAll(Optional.ofNullable(portfolio.getPortfolioToSymbols()).orElseGet(() -> Set.of()).stream()
						.flatMap(pts -> Stream.of(this.symbolMapper.convert(pts.getSymbol(), pts)))
						.collect(Collectors.toList()));
		List<PortfolioElementDto> myPortfolioElements = Optional.ofNullable(portfolioElements).orElse(List.of())
				.stream().filter(StreamHelpers.distinctByKey(myElement -> myElement.getSymbol()))
				.map(this::toPortfolioElementDto).toList();
		if (!portfolioElements.isEmpty()) {
			dto.getPortfolioElements().addAll(myPortfolioElements);
		}
		return dto;
	}

	@SuppressWarnings("unchecked")
	private <T> T getValue(String methodName, Class<T> returnClass, Object myObject) {
		T result;
		try {
			var method = myObject.getClass().getMethod(methodName);
			result = (T) method.invoke(myObject);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private void setValue(String methodName, Optional<Object> value, Object myObject) {
		value.ifPresent(myValue -> {
			try {
				var method = myObject.getClass().getMethod(methodName, myValue.getClass());
				method.invoke(myObject, myValue);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		});
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