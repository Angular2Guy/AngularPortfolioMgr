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
package ch.xxx.manager;

import java.io.IOException;
import java.util.List;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

public class ComponentScanCustomFilter implements TypeFilter, EnvironmentAware {
	private Environment environment;

	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
			throws IOException {
		String searchString = List.of(this.environment.getActiveProfiles()).stream()
				.anyMatch(profileStr -> profileStr.contains("prod")) ? "ch.xxx.manager.dev." : "ch.xxx.manager.prod.";
		ClassMetadata classMetadata = metadataReader.getClassMetadata();
		return classMetadata.getClassName().contains(searchString);
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

}
