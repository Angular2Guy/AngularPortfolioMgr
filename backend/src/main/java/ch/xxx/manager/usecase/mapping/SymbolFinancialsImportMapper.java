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

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.entity.FinancialElement;
import ch.xxx.manager.domain.model.entity.SymbolFinancials;
import ch.xxx.manager.domain.model.entity.dto.FinancialElementImportDto;
import ch.xxx.manager.domain.model.entity.dto.FinancialsDataDto;
import ch.xxx.manager.domain.model.entity.dto.SymbolFinancialsDto;
import ch.xxx.manager.domain.utils.DataHelper;

@Component
public class SymbolFinancialsImportMapper {
	private final FinancialElementImportMapper financialElementMapper;

	public SymbolFinancialsImportMapper(FinancialElementImportMapper financialElementMapper) {
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
		financialsDataDto.setBalanceSheet(symbolFinancials.getFinancialElements().stream()
				.filter(myElment -> DataHelper.FinancialElementType.BalanceSheet
						.equals(myElment.getFinancialElementType()))
				.map(myElement -> this.financialElementMapper.toDto(myElement))
				.map(myElement -> addSymbolFinancialsDto(dto, myElement)).collect(Collectors.toSet()));
		financialsDataDto.setCashFlow(
				symbolFinancials.getFinancialElements().stream()
				.filter(myElment -> DataHelper.FinancialElementType.CashFlow
						.equals(myElment.getFinancialElementType()))
				.map(myElement -> this.financialElementMapper.toDto(myElement))
						.map(myElement -> addSymbolFinancialsDto(dto, myElement)).collect(Collectors.toSet()));
		financialsDataDto.setIncome(
				symbolFinancials.getFinancialElements().stream()
				.filter(myElment -> DataHelper.FinancialElementType.Income
						.equals(myElment.getFinancialElementType()))
				.map(myElement -> this.financialElementMapper.toDto(myElement))
						.map(myElement -> addSymbolFinancialsDto(dto, myElement)).collect(Collectors.toSet()));
		dto.setData(financialsDataDto);
		return dto;
	}

	private FinancialElementImportDto addSymbolFinancialsDto(SymbolFinancialsDto dto,
			FinancialElementImportDto myElement) {
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
			Set<FinancialElement> financialElements = symbolFinancialsDto.getData().getBalanceSheet().stream()
					.map(myElement -> this.financialElementMapper.toEntity(myElement, DataHelper.FinancialElementType.BalanceSheet))
					.map(myElement -> addSymbolFinancialsEntity(entity, myElement)).collect(Collectors.toSet());
			financialElements.addAll(symbolFinancialsDto.getData().getCashFlow().stream()
					.map(myElement -> this.financialElementMapper.toEntity(myElement, DataHelper.FinancialElementType.CashFlow))
					.map(myElement -> addSymbolFinancialsEntity(entity, myElement)).collect(Collectors.toSet()));
			financialElements.addAll(symbolFinancialsDto.getData().getIncome().stream()
					.map(myElement -> this.financialElementMapper.toEntity(myElement, DataHelper.FinancialElementType.Income))
					.map(myElement -> addSymbolFinancialsEntity(entity, myElement)).collect(Collectors.toSet()));
			entity.getFinancialElements().addAll(financialElements);
		}
		return entity;
	}

	private FinancialElement addSymbolFinancialsEntity(SymbolFinancials entity, FinancialElement myElement) {
		myElement.setSymbolFinancials(entity);
		return myElement;
	}
}
