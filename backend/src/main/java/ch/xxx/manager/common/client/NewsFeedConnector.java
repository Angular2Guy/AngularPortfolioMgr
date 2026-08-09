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
package ch.xxx.manager.common.client;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import ch.xxx.manager.stocks.dto.CompanyReportWrapper;
import ch.xxx.manager.stocks.entity.dto.RssDto;
import ch.xxx.manager.stocks.mapping.open.NewsFeedMapper;
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
    private final AtomicLong nextAllowedRequestTime = new AtomicLong(System.currentTimeMillis());
    private static final long REQUEST_INTERVAL_MS = 1000;
    @Value("${sec.useragent:}")
    private String userAgent;

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

    private <T> T loadFile(String sourceUrl, Class<T> classType) {
        var url = Optional.ofNullable(sourceUrl).orElseThrow();
        var isSecRequest = url.toLowerCase().contains("sec.gov");
        var client = this.restClient.get().uri(url)
                .header("Accept-Encoding", "gzip, deflate");
        if (isSecRequest) {
            this.acquireToken();
            client.header("Host", Optional.ofNullable(URI.create(url).getHost()).orElseThrow())
                    .header("User-Agent", this.userAgent);
        }
        var result = client.retrieve().body(classType);
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

    private void acquireToken() {
        long now = System.currentTimeMillis();
        long targetTime = nextAllowedRequestTime.getAndUpdate(existing -> Math.max(existing, now) + REQUEST_INTERVAL_MS);

        long delay = targetTime - now;
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Unexpected interruption of ratelimiting.", e);
            }
        }
    }
}
