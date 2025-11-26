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
package ch.xxx.manager.adapter.client;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import ch.xxx.manager.domain.model.dto.SymbolToCikWrapperDto;
import ch.xxx.manager.domain.model.dto.SymbolToCikWrapperDto.CompanySymbolDto;
import ch.xxx.manager.usecase.service.SecSymbolClient;

@Component
public class SecSymbolConnector implements SecSymbolClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecSymbolConnector.class);
	private final ConnectorClient connectorClient;
    private static final String URL_STRING = "https://www.sec.gov/files/company_tickers.json";

    public SecSymbolConnector(ConnectorClient connectorClient) {
        this.connectorClient = connectorClient;
    }

    @Override
    public Map<String, CompanySymbolDto> importSymbols() {
    	MultiValueMap<String, String> headerValues = new MultiValueMapAdapter<String, String>(new HashMap<>());
    	headerValues.add("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
    	headerValues.add("accept-encoding", "gzip");
    	headerValues.add("accept-language", "en-US,en;q=0.9");
    	headerValues.add("priority", "u=0, i");
    	headerValues.add("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Chromium\";v=\"142\"\n");
    	headerValues.add("sec-ch-ua-mobile", "\n"+ "?0");
    	headerValues.add("sec-ch-ua-platform", "\"Linux\"");
    	headerValues.add("sec-fetch-dest", "document");
    	headerValues.add("sec-fetch-mode", "navigate");
    	headerValues.add("sec-fetch-site", "none");
    	headerValues.add("sec-fetch-user", "?1");
    	headerValues.add("upgrade-insecure-requests", "1");
    	headerValues.add("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36");
        return this.connectorClient.restCall(URL_STRING, headerValues, new ParameterizedTypeReference<Map<String, SymbolToCikWrapperDto.CompanySymbolDto>>() {}, Duration.ZERO)
          .stream().findFirst().orElseThrow(() -> new RuntimeException("Error while importing symbols"));        
    }
}
