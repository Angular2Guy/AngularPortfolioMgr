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
package ch.xxx.manager.init;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class InitApplication {
	private static final Logger logger = LoggerFactory.getLogger(InitApplication.class);
	@Autowired
	private DatabaseClient database;
	@Value("${DB_URL:}")
	private String dbUrl;

	@PostConstruct
	private void init() {
		if (this.dbUrl == null || this.dbUrl.isBlank()) {
			List<String> statements = Arrays.asList("create sequence mainseq start with 1000 increment by 100",
					"create table appuser (id bigint identity primary key, name varchar(50), firstname varchar(50), birthdate date)");
			statements.forEach(it -> database.execute(it).fetch().rowsUpdated().block());
			statements = Arrays
					.asList("insert into appuser (name, firstname, birthdate) values ('Max', 'Smith', now())");
			statements.forEach(it -> database.execute(it).fetch().rowsUpdated().block());
			logger.info("Init finished.");
		}
	}
}
