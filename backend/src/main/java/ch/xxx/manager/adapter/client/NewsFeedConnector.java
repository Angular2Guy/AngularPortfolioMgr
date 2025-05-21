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
import java.util.List;
import java.util.Optional;

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

import ch.xxx.manager.domain.model.entity.dto.CompanyReportWrapper;
import ch.xxx.manager.domain.model.entity.dto.RssDto;
import ch.xxx.manager.usecase.mapping.NewsFeedMapper;
import ch.xxx.manager.usecase.service.NewsFeedClient;

@Component
public class NewsFeedConnector implements NewsFeedClient {

    private static final String SEEKING_ALPHA_URL = "https://seekingalpha.com/market_currents.xml";
    private static final String CNBC_FINANCE_URL = "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000664";
    private static final String SEC_EDGAR_USGAAP = "https://www.sec.gov/Archives/edgar/usgaap.rss.xml";
    private static final String SEC_EDGAR_ICON = "https://www.sec.gov/favicon.icon";
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsFeedConnector.class);
    private final RestClient restClient;
    private final XmlMapper xmlMapper;
    private final NewsFeedMapper newsFeedMapper;

    public NewsFeedConnector(RestClient restClient, @Qualifier("xml") XmlMapper xmsMapper, NewsFeedMapper newsFeedMapper) {
        this.restClient = restClient;
        this.xmlMapper = xmsMapper;
        this.newsFeedMapper = newsFeedMapper;
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
    public List<CompanyReportWrapper> importSecEdgarUsGaapNewsFeed() {
        //var result = this.loadFile(SEC_EDGAR_USGAAP, String.class);
        record ResultItem(String content, Boolean icon) {}
        var result = List.of(SEC_EDGAR_USGAAP, SEC_EDGAR_ICON).stream().parallel()
          .map(url -> new ResultItem(this.loadFile(url, String.class, SEC_EDGAR_ICON.equals(url)), SEC_EDGAR_ICON.equals(url)))
          .filter(item -> !item.icon).findFirst().map(item -> item.content).orElseThrow(() -> new IllegalStateException("No content found"));
        RssDto rssDto = null;		
		try {
            rssDto = this.xmlMapper.readValue(result, RssDto.class);
            //LOGGER.info("Xml length: "+this.xmlMapper.writeValueAsString(rssDto).length());
            //LOGGER.info("Xml mapping successful");
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to parse XML response", ex);
        }
		
        return Optional.ofNullable(rssDto)
                .map(myRssDto -> this.newsFeedMapper.convert(
                    myRssDto))
                .orElse(List.of());
    }

    @Override
    public byte[] loadCompanyReportZip(String url) {
        return this.loadFile(url, byte[].class, false);
    }

    private <T> T loadFile(String url, Class<T> classType, Boolean icon) {
        var result = this.restClient.get().uri(url)                
                .header("Accept-Encoding", "gzip, deflate")    
                .header("Host", "www.sec.gov")
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0")
                .header("Accept", icon ?  "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5" : "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .header(icon ? "Referer" : "Upgrade-Insecure-Requests", icon ? "https://www.sec.gov/Archives/edgar/usgaap.rss.xml" : "1")
                .header("Sec-Fetch-Dest",  icon ? "image" : "document")
                .header("Sec-Fetch-Mode", icon ? "no-cors" : "navigate")
                .header("Sec-Fetch-Site", icon ? "same-origin" : "none")
                .header(icon ? "TE" : "Sec-Fetch-User", icon ? "trailers" : "?1")
                .header("Priority", icon ? "u=6" : "u=0, i")
                .retrieve().body(classType);
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
