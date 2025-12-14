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
package ch.xxx.manager.stocks.mapping;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.xxx.manager.findata.mapping.FinancialElementMapper;
import org.springframework.stereotype.Component;

import ch.xxx.manager.stocks.dto.SymbolFinancialsDto;
import ch.xxx.manager.stocks.dto.SymbolNameDto;
import ch.xxx.manager.stocks.entity.SymbolFinancials;

@Component
public class SymbolFinancialsMapper {
	private final FinancialElementMapper financialElementMapper;

	public SymbolFinancialsMapper(FinancialElementMapper financialElementMapper) {
		this.financialElementMapper = financialElementMapper;
	}

	public SymbolFinancialsDto toDto(SymbolFinancials symbolFinancials) {
		SymbolFinancialsDto dto = new SymbolFinancialsDto();
		dto.setEndDate(symbolFinancials.getEndDate());
		dto.setQuarter(symbolFinancials.getQuarter());
		dto.setStartDate(symbolFinancials.getStartDate());
		dto.setSymbol(symbolFinancials.getSymbol());
		dto.setYear(symbolFinancials.getFiscalYear());
		dto.setId(symbolFinancials.getId());
		dto.setCity(symbolFinancials.getCity());
		dto.setName(symbolFinancials.getName());
		dto.setCountry(symbolFinancials.getCountry());
		dto.setFinancialElements(Optional.ofNullable(symbolFinancials.getFinancialElements()).stream().flatMap(Set::stream)
				.map(this.financialElementMapper::toDto).collect(Collectors.toList()));
		return dto;
	}
	
	public SymbolNameDto toRc(SymbolFinancials symbolFinancials) {
		return new SymbolNameDto(symbolFinancials.getSymbol(), symbolFinancials.getName());
	}
}
