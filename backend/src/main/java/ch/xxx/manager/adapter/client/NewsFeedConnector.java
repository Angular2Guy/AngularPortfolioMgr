/**
 *    Copyright 2019 Sven Loesekann
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
package ch.xxx.manager.adapter.client;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import ch.xxx.manager.domain.model.dto.RssDto;
import ch.xxx.manager.usecase.service.NewsFeedClient;

@Component
public class NewsFeedConnector implements NewsFeedClient {

    private static final String SEEKING_ALPHA_URL = "https://seekingalpha.com/market_currents.xml";
    private static final String CNBC_FINANCE_URL = "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000664";
    private static final String SEC_EDGAR_USGAAP = "https://www.sec.gov/Archives/edgar/usgaap.rss.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsFeedConnector.class);
    private final RestClient restClient;
    private final XmlMapper xmlMapper;

    public NewsFeedConnector(RestClient restClient, @Qualifier("xml") XmlMapper xmsMapper) {
        this.restClient = restClient;
        this.xmlMapper = xmsMapper;
    }

    @Override
    public SyndFeed importSeekingAlphaFeed() {
        return this.importNewsFeed(SEEKING_ALPHA_URL);
    }

    @Override
    public SyndFeed importCnbcFinanceNewsFeed() {
        return this.importNewsFeed(CNBC_FINANCE_URL);
    }

    @Override
    public String importSecEdgarUsGaapNewsFeed() {
        var result = this.restClient.get().uri(SEC_EDGAR_USGAAP)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("accept-language", "en-US,en;q=0.9")
                .header("priority", "u=0, i")
                .header("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                .header("upgrade-insecure-requests", "1")
                .header("sec-fetch-user", "?1")
                .header("sec-fetch-site", "none")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-dest", "document")
                .header("sec-ch-ua-platform", "Linux")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua", "Not.A/Brand;v=99", "Chromium;v=136")
                .retrieve().body(String.class);
        RssDto rssDto = null;		
		try {
            rssDto = this.xmlMapper.readValue(result, RssDto.class);
            LOGGER.info("Xml length: "+this.xmlMapper.writeValueAsString(rssDto).length());
            LOGGER.info("Xml mapping successful");
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to parse XML response", ex);
        }
		
        return result;
    }

    private SyndFeed importNewsFeed(String url) {
        SyndFeed feed = null;
        try {
            feed = new SyndFeedInput().build(new XmlReader(URI.create(url).toURL().openStream()));
        } catch (FeedException | IOException | IllegalArgumentException e) {
            LOGGER.error(String.format("Feed import failed. url: %s", url), e);
        }
        return feed;
    }
}
