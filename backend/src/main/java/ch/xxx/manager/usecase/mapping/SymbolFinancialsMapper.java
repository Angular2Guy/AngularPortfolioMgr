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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.SymbolFinancialsDto;
import ch.xxx.manager.domain.model.entity.SymbolFinancials;

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
		dto.setYear(symbolFinancials.getYear());
		dto.setFinancialElementDtos(Optional.ofNullable(symbolFinancials.getFinancialElements()).stream().flatMap(Set::stream)
				.map(myFe -> this.financialElementMapper.toDto(myFe)).collect(Collectors.toList()));
		return dto;
	}
}
