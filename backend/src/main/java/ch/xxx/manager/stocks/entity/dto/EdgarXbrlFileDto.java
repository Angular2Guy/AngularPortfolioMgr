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
package ch.xxx.manager.stocks.entity.dto;


import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "xbrlFile", namespace = "edgar")
public record EdgarXbrlFileDto(
        @JacksonXmlProperty(isAttribute = true, localName = "sequence", namespace = "edgar") String sequence,
        @JacksonXmlProperty(isAttribute = true, localName = "file", namespace = "edgar") String file,
        @JacksonXmlProperty(isAttribute = true, localName = "type", namespace = "edgar") String type,
        @JacksonXmlProperty(isAttribute = true, localName = "size", namespace = "edgar") String size,
        @JacksonXmlProperty(isAttribute = true, localName = "description", namespace = "edgar") String description,
        @JacksonXmlProperty(isAttribute = true, localName = "inlineXBRL", namespace = "edgar") String inlineXBRL,
        @JacksonXmlProperty(isAttribute = true, localName = "url", namespace = "edgar") String url) {
}
