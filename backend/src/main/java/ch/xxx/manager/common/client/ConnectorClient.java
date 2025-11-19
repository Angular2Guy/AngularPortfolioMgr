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
package ch.xxx.manager.common.client;

import java.time.Duration;
import java.util.Optional;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;

@Component
public class ConnectorClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorClient.class);
	private final RestClient restClient;

	public ConnectorClient() {
		var requestConfig = RequestConfig.custom().setResponseTimeout(Timeout.ofMilliseconds(10000)).build();
		var httpClient = HttpClientBuilder.create()
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(1, TimeValue.ofSeconds(1)))
				.setDefaultRequestConfig(requestConfig).build();
		var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		factory.setConnectTimeout(5000);
		factory.setConnectionRequestTimeout(5000);
		this.restClient = RestClient.builder().requestFactory(factory).build();
	}

	public <T> Optional<T> restCall(String url, Class<T> typeClass, Duration delay) {
		return restCall(url, new LinkedMultiValueMap<String, String>(), typeClass, delay);
	}
	
	public <T> Optional<T> restCall(String url, Class<T> typeClass) {
		return restCall(url, new LinkedMultiValueMap<>(), typeClass, Duration.ZERO);
	}

	public <T> Optional<T> restCall(String url, ParameterizedTypeReference<T> valueTypeRef) {
		return restCall(url, new LinkedMultiValueMap<String, String>(), valueTypeRef);
	}

	public <T> Optional<T> restCall(String url, MultiValueMap<String, String> headerValues, Class<T> typeClass, Duration delay) {
		return Optional.ofNullable(createCall(url, headerValues, delay).toEntity(typeClass).getBody());
	}

	public <T> Optional<T> restCall(String url, MultiValueMap<String, String> headerValues,
			ParameterizedTypeReference<T> valueTypeRef) {
		return Optional.ofNullable(createCall(url, headerValues, Duration.ZERO).body(valueTypeRef));
	}

	public <T> Optional<T> restCall(String url, MultiValueMap<String, String> headerValues,
			ParameterizedTypeReference<T> valueTypeRef, Duration delay) {
		return Optional.ofNullable(createCall(url, headerValues, delay).body(valueTypeRef));
	}
	
	private ResponseSpec createCall(String url, MultiValueMap<String, String> headerValues, Duration delay) {
		var myDelay = Optional.ofNullable(delay).orElse(Duration.ZERO);
		if (!Duration.ZERO.equals(myDelay)) {
			try {
				LOGGER.info("Sleeping for {}", myDelay.toMillis());
				Thread.sleep(myDelay);				
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return this.restClient.get().uri(url).headers(headers -> headers.addAll(headerValues)).retrieve();
	}
}
