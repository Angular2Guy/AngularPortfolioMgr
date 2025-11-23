/**
 * Copyright 2019 Sven Loesekann
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.xxx.manager.domain.model.entity.dto;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;


@JacksonXmlRootElement(localName = "xbrlFiling", namespace = "edgar")
public record EdgarXbrlFilingDto(@JacksonXmlProperty(localName = "companyName", namespace = "edgar") String companyName,
                                 @JacksonXmlProperty(localName = "formType", namespace = "edgar") String formType,
                                 @JacksonXmlProperty(localName = "filingDate", namespace = "edgar") String filingDate,
                                 @JacksonXmlProperty(localName = "otherCikNumbers", namespace = "edgar") String otherCikNumbers,
                                 @JacksonXmlProperty(localName = "cikNumber", namespace = "edgar") String cikNumber,
                                 @JacksonXmlProperty(localName = "accessionNumber", namespace = "edgar") String accessionNumber,
                                 @JacksonXmlProperty(localName = "fileNumber", namespace = "edgar") String fileNumber,
                                 @JacksonXmlProperty(localName = "acceptanceDatetime", namespace = "edgar") String acceptanceDatetime,
                                 @JacksonXmlProperty(localName = "period", namespace = "edgar") String period,
                                 @JacksonXmlProperty(localName = "assistantDirector", namespace = "edgar") String assistantDirector,
                                 @JacksonXmlProperty(localName = "assignedSic", namespace = "edgar") String assignedSic,
                                 @JacksonXmlProperty(localName = "fiscalYearEnd", namespace = "edgar") String fiscalYearEnd,
                                 @JacksonXmlElementWrapper(localName = "xbrlFiles", namespace = "edgar") List<EdgarXbrlFileDto> xbrlFiles) {
}
