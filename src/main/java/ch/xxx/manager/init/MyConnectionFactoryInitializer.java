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

import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.DatabasePopulator;
import org.springframework.data.r2dbc.connectionfactory.init.DatabasePopulatorUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.r2dbc.spi.ConnectionFactory;

/**
 * Needed fix for Springs ConnectionFactoryInitializer.
 *  
 * @author sven1
 */
public class MyConnectionFactoryInitializer extends ConnectionFactoryInitializer {
	private @Nullable ConnectionFactory connectionFactory;

	private @Nullable DatabasePopulator databasePopulator;

	private @Nullable DatabasePopulator databaseCleaner;

	private boolean enabled = true;
	
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setDatabasePopulator(DatabasePopulator databasePopulator) {
		this.databasePopulator = databasePopulator;
	}

	public void setDatabaseCleaner(DatabasePopulator databaseCleaner) {
		this.databaseCleaner = databaseCleaner;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public void afterPropertiesSet() {
		execute(this.databasePopulator);
	}

	@Override
	public void destroy() {
		execute(this.databaseCleaner);
	}

	private void execute(@Nullable DatabasePopulator populator) {

		Assert.state(this.connectionFactory != null, "ConnectionFactory must be set");

		if (this.enabled && populator != null) {
			DatabasePopulatorUtils.execute(populator, this.connectionFactory).block();
		}
	}
}
