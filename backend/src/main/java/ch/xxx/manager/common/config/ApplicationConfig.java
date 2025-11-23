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
package ch.xxx.manager.common.config;

import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import jakarta.annotation.PostConstruct;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import tools.jackson.dataformat.csv.CsvMapper;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT2H")
@EnableAsync
public class ApplicationConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

	@Value("${api.key}")
	private String alphavantageApiKey;
	@Value("${api.key.rapidapi}")
	private String rapidApiKey;

	@PostConstruct
	public void init() {
		Optional.ofNullable(this.alphavantageApiKey).filter(myApiKey -> !myApiKey.isBlank() || myApiKey.equals("xxx")).orElseGet(() -> {
			LOGGER.warn("Alphavantage Api Key is missing!");
			return null;
		});
		Optional.ofNullable(this.rapidApiKey).filter(myApiKey -> !myApiKey.isBlank() || myApiKey.equals("yyy")).orElseGet(() -> {
			LOGGER.warn("RapidApi Api Key is missing!");
			return null;
		});
	}
	
	@Bean
	public LockProvider lockProvider(DataSource dataSource) {
		return new JdbcTemplateLockProvider(dataSource);
	}

	@Bean
	public RestClient restClient() {
		return RestClient.create();
	}
}