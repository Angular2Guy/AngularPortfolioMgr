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

import java.util.Optional;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;

import com.fasterxml.jackson.core.type.TypeReference;

public class ConnectorUtils {

	public static <T> Optional<T> restCall(String url, MultiValueMap<String, String> headerValues, Class<T> typeClass) {
		return Optional.ofNullable(createCall(url, headerValues).toEntity(typeClass).getBody());
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> restCall(String url, MultiValueMap<String, String> headerValues,
			TypeReference<T> valueTypeRef) {		
		return Optional.ofNullable(((T) createCall(url, headerValues).toEntity(valueTypeRef.getClass()).getBody()));
	}

	private static ResponseSpec createCall(String url, MultiValueMap<String, String> headerValues) {
		var factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(5000);
		factory.setConnectionRequestTimeout(5000);
		return RestClient.builder().requestFactory(factory).build().get().uri(url)
				.headers(headers -> headers.addAll(headerValues)).retrieve();
	}
}
