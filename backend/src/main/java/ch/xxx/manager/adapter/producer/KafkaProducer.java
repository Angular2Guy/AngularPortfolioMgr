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
package ch.xxx.manager.adapter.producer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import ch.xxx.manager.adapter.config.KafkaConfig;
import ch.xxx.manager.domain.exception.AuthenticationException;

@Service
@Profile({"kafka","prod-kafka"})
public class KafkaProducer {
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);
	private final KafkaTemplate<String,String> kafkaTemplate;
	
	public KafkaProducer(KafkaTemplate<String,String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendLogoutMsg() {
		String msg = "{}";
		ListenableFuture<SendResult<String,String>> listenableFuture = this.kafkaTemplate.send(KafkaConfig.USER_LOGOUT_TOPIC, msg);
		try {
			listenableFuture.get(2, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new AuthenticationException("Logout failed.");
		}
		LOGGER.info("send logout msg: {}", msg);
	}
	
	public void sendNewUserMsg() {
		String msg = "{}";
		ListenableFuture<SendResult<String,String>> listenableFuture = this.kafkaTemplate.send(KafkaConfig.NEW_USER_TOPIC, msg);
		try {
			listenableFuture.get(2, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new AuthenticationException("User creation failed.");
		}
		LOGGER.info("send new user msg: {}", msg);
	}
}
