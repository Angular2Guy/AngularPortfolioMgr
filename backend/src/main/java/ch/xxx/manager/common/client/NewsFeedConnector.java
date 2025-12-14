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
package ch.xxx.manager.common.client;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import ch.xxx.manager.stocks.entity.dto.CompanyReportWrapper;
import ch.xxx.manager.stocks.entity.dto.RssDto;
import ch.xxx.manager.stocks.mapping.NewsFeedMapper;
import ch.xxx.manager.stocks.NewsFeedClient;
import tools.jackson.dataformat.xml.XmlMapper;

@Component
public class NewsFeedConnector implements NewsFeedClient {

    private static final String SEEKING_ALPHA_URL = "https://seekingalpha.com/market_currents.xml";
    private static final String CNBC_FINANCE_URL = "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000664";
    private static final String SEC_EDGAR_USGAAP = "https://www.sec.gov/Archives/edgar/usgaap.rss.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsFeedConnector.class);
    private final RestClient restClient;
    private final XmlMapper xmlMapper;
    private final NewsFeedMapper newsFeedMapper;

    public NewsFeedConnector(RestClient restClient, XmlMapper xmsMapper, NewsFeedMapper newsFeedMapper) {
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
        var result = this.loadFile(SEC_EDGAR_USGAAP, String.class);        
        RssDto rssDto = null;
        rssDto = this.xmlMapper.readValue(result, RssDto.class);
        //LOGGER.info("Xml length: "+this.xmlMapper.writeValueAsString(rssDto).length());
        //LOGGER.info("Xml mapping successful");

        return Optional.ofNullable(rssDto)
                .map(this.newsFeedMapper::convert)
                .orElse(List.of());
    }

    @Override
    public byte[] loadCompanyReportZip(String url) {
        return this.loadFile(url, byte[].class);
    }

    private <T> T loadFile(String url, Class<T> classType) {
        var result = this.restClient.get().uri(url)                
                .header("Accept-Encoding", "gzip, deflate")    
                .header("Host", "www.sec.gov")
                .header("User-Agent","Sven Loesekann support@svenloesekann.de")              
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
