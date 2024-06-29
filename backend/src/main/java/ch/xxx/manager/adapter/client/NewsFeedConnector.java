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

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import ch.xxx.manager.usecase.service.NewsFeedClient;

@Component
public class NewsFeedConnector implements NewsFeedClient {
	public static final String YAHOO_FINANCE_URL = "https://finance.yahoo.com/news/rssindex";
	public static final String CNBC_FINANCE_URL = "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000664";
	private static final Logger LOGGER = LoggerFactory.getLogger(NewsFeedConnector.class);
	
	@Override
	public SyndFeed importYahooNewsFeed() {
		return this.importNewsFeed(YAHOO_FINANCE_URL);
	}
	
	@Override
	public SyndFeed importCnbcFinanceNewsFeed() {
		return this.importNewsFeed(CNBC_FINANCE_URL);
	}
	
	private SyndFeed importNewsFeed(String url) {
		SyndFeed feed = null;
		try {
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(new XmlReader(URI.create(url).toURL().openStream()));
		} catch (Exception e) {
			LOGGER.error(String.format("Feed import failed. url: %s", url),e);
		}
		return feed;
	}
}
