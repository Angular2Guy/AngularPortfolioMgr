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
package ch.xxx.manager.domain.model.dto;

import ch.xxx.manager.domain.utils.DataHelper.Operation;
import ch.xxx.manager.domain.utils.DataHelper.TermType;
import jakarta.validation.constraints.NotNull;

public class FinancialElementParamDto  {
	@NotNull
	private FilterStringDto conceptFilter;
	@NotNull
	private FilterNumberDto valueFilter;
	private Operation operation;
	private TermType termType;

	public FilterStringDto getConceptFilter() {
		return conceptFilter;
	}
	public void setConceptFilter(FilterStringDto conceptFilter) {
		this.conceptFilter = conceptFilter;
	}
	public FilterNumberDto getValueFilter() {
		return valueFilter;
	}
	public void setValueFilter(FilterNumberDto valueFilter) {
		this.valueFilter = valueFilter;
	}
	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	public TermType getTermType() {
		return termType;
	}
	public void setTermType(TermType termType) {
		this.termType = termType;
	}
}
