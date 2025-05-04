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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.CompanyToSymbolWrapperDto;

@Component
public class SecSymbolConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecSymbolConnector.class);
	private final ConnectorClient connectorClient;
    private static final String URL_STRING = "https://www.sec.gov/files/company_tickers.json";

    public SecSymbolConnector(ConnectorClient connectorClient) {
        this.connectorClient = connectorClient;
    }

    public void importSymbols() {
        this.connectorClient.restCall(URL_STRING, CompanyToSymbolWrapperDto.class)
            .ifPresent(response -> {                
                LOGGER.info("Successfully imported symbols: " + response.getCompanies().size());
            });

    }
}
