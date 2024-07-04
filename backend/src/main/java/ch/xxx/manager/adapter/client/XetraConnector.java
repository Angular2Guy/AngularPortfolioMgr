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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.xxx.manager.usecase.service.XetraClient;

@Component
public class XetraConnector implements XetraClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(XetraConnector.class);
	private static final String XETRA_URL = "https://www.xetra.com/xetra-de/instrumente/alle-handelbaren-instrumente";
	private final ConnectorClient connectorClient;
	
	public XetraConnector(ConnectorClient connectorClient) {
		this.connectorClient = connectorClient;
	}

	@Override
	public Optional<List<String>> importXetraSymbols() {
		return this.connectorClient
				.restCall(XETRA_URL, String.class).stream().map(str -> this.findCsvUrl(str)).findFirst()
				.flatMap(myUrlStr -> this.loadSymbolsCsv(myUrlStr));
	}


	private Optional<List<String>> loadSymbolsCsv(String url) {
		return this.connectorClient.restCall(url, String.class).stream().map(str -> str.lines().toList()).findFirst();
	}

	private String findCsvUrl(String htmlPage) {
		// find 'href="..."' in html page
		Pattern pattern = Pattern.compile("(href=\\\"(.*?)\\\")");
		Matcher matcher = pattern.matcher(htmlPage);
		List<String> hrefs = new ArrayList<String>();
		while (matcher.find()) {
			hrefs.add(matcher.group(1));
		}
		// create csv url for the xetra stocks
		String csvUrl = hrefs.stream().filter(href -> href.contains("allTradableInstruments.csv"))
				.map(href -> this.createCsvUrl(href)).findFirst()
				.orElseThrow(() -> new RuntimeException("allTradableInstruments.csv not found."));
		LOGGER.info(csvUrl);
		return csvUrl;
	}

	private String createCsvUrl(String href) {
		String url = (href.contains("https://www.xetra.com") ? "" : "https://www.xetra.com")
				+ href.replaceAll("href=\"", "").replaceAll("\"", "");
		return url;
	}
}
