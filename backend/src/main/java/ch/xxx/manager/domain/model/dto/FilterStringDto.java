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

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.NotNull;

public class FilterStringDto {
	public static enum Operation {StartsWith("=*"), EndsWith("*="), Equal("="), Contains("*=*");
		private String value;
		
		private Operation(String value) {
			this.value = value;
		}
		@JsonValue
		public String toString() {
			return this.value;
		}
	}
	@NotNull
	private Operation operation;
	private String value;

	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
