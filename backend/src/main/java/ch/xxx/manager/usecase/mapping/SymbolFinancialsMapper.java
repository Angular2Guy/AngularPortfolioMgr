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

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.entity.FinancialElement;
import ch.xxx.manager.domain.model.entity.SymbolFinancials;
import ch.xxx.manager.domain.model.entity.dto.FinancialElementDto;
import ch.xxx.manager.domain.model.entity.dto.FinancialsDataDto;
import ch.xxx.manager.domain.model.entity.dto.SymbolFinancialsDto;

@Component
public class SymbolFinancialsMapper {
	private final FinancialElementMapper financialElementMapper;

	public SymbolFinancialsMapper(FinancialElementMapper financialElementMapper) {
		this.financialElementMapper = financialElementMapper;
	}

	public SymbolFinancialsDto toDto(SymbolFinancials symbolFinancials) {
		SymbolFinancialsDto dto = new SymbolFinancialsDto();
		dto.setEndDate(symbolFinancials.getEndDate());
		dto.setId(symbolFinancials.getId());
		dto.setQuarter(symbolFinancials.getQuarter());
		dto.setStartDate(symbolFinancials.getStartDate());
		dto.setSymbol(symbolFinancials.getSymbol());
		dto.setYear(symbolFinancials.getYear());
		FinancialsDataDto financialsDataDto = new FinancialsDataDto();
		financialsDataDto.setBalanceSheet(symbolFinancials.getBalanceSheet().stream()
				.map(myElement -> this.financialElementMapper.toDto(myElement))
				.map(myElement -> addSymbolFinancialsDto(dto, myElement)).collect(Collectors.toSet()));
		financialsDataDto.setCashFlow(
				symbolFinancials.getCashFlow().stream().map(myElement -> this.financialElementMapper.toDto(myElement))
						.map(myElement -> addSymbolFinancialsDto(dto, myElement)).collect(Collectors.toSet()));
		financialsDataDto.setIncome(
				symbolFinancials.getIncome().stream().map(myElement -> this.financialElementMapper.toDto(myElement))
						.map(myElement -> addSymbolFinancialsDto(dto, myElement)).collect(Collectors.toSet()));
		dto.setData(financialsDataDto);
		return dto;
	}

	private FinancialElementDto addSymbolFinancialsDto(SymbolFinancialsDto dto, FinancialElementDto myElement) {
		myElement.setSymbolFinancialsDto(dto);
		return myElement;
	}

	public SymbolFinancials toEntity(SymbolFinancialsDto symbolFinancialsDto) {
		SymbolFinancials entity = new SymbolFinancials();
		entity.setEndDate(symbolFinancialsDto.getEndDate());
		entity.setId(symbolFinancialsDto.getId());
		entity.setQuarter(symbolFinancialsDto.getQuarter());
		entity.setStartDate(symbolFinancialsDto.getStartDate());
		entity.setSymbol(symbolFinancialsDto.getSymbol());
		entity.setYear(symbolFinancialsDto.getYear());
		if (symbolFinancialsDto.getData() != null) {
			entity.setBalanceSheet(symbolFinancialsDto.getData().getBalanceSheet().stream()
					.map(myElement -> this.financialElementMapper.toEntity(myElement))
					.map(myElement -> addSymbolFinancialsEntity(entity, myElement)).collect(Collectors.toSet()));
			entity.setCashFlow(symbolFinancialsDto.getData().getCashFlow().stream()
					.map(myElement -> this.financialElementMapper.toEntity(myElement))
					.map(myElement -> addSymbolFinancialsEntity(entity, myElement)).collect(Collectors.toSet()));
			entity.setIncome(symbolFinancialsDto.getData().getIncome().stream()
					.map(myElement -> this.financialElementMapper.toEntity(myElement))
					.map(myElement -> addSymbolFinancialsEntity(entity, myElement)).collect(Collectors.toSet()));
		}
		return entity;
	}

	private FinancialElement addSymbolFinancialsEntity(SymbolFinancials entity, FinancialElement myElement) {
		myElement.setSymbolFinancials(entity);
		return myElement;
	}
}
