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
package ch.xxx.manager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.xxx.manager.init.MyConnectionFactoryInitializer;
import io.r2dbc.proxy.ProxyConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableR2dbcRepositories
@EnableSwagger2
@EnableScheduling
@EnableTransactionManagement
class ApplicationConfig extends AbstractR2dbcConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

	@Value("${spring.profiles.active:}")
	private String activeProfile;
	
	@Bean
	public ObjectMapper createObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}
	
	@Override
	@Bean
	public ConnectionFactory connectionFactory() {
		if (this.activeProfile.contains("prod")) {
			return this.connectionFactory();
		} else {
			ConnectionFactory connectionFactory = ConnectionFactories
					.get("r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;");

			return ProxyConnectionFactory.builder(connectionFactory)
//					.onAfterQuery(
//							queryExecInfo -> LOGGER.info(QueryExecutionInfoFormatter.showAll().format(queryExecInfo)))
					.build();
		}
	}

	@Bean
	public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		MyConnectionFactoryInitializer initializer = new MyConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		if (!this.activeProfile.contains("prod")) {
			CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
			populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("/local/schema.sql")));
			populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("/local/data.sql")));
			initializer.setDatabasePopulator(populator);
		}

		return initializer;
	}	
	
	@Bean
	public ServerCodecConfigurer serverCodecConfigurer() {
		return new DefaultServerCodecConfigurer();
	}

	@Bean
	public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
		return new R2dbcTransactionManager(connectionFactory);
	}
}