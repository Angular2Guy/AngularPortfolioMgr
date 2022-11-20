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

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.SqlResultSetMapping;

@SqlResultSetMapping(name = "Mapping.FeConceptDto", classes = @ConstructorResult(targetClass = FeConceptDto.class, columns = {
		@ColumnResult(name = "concept_count"), @ColumnResult(name = "concept") }))
public class FeConceptDto {
	private String concept;
	private int timesFound;

	public FeConceptDto(String concept, int timesFound) {
		super();
		this.concept = concept;
		this.timesFound = timesFound;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public int getTimesFound() {
		return timesFound;
	}

	public void setTimesFound(int timesFound) {
		this.timesFound = timesFound;
	}
}
