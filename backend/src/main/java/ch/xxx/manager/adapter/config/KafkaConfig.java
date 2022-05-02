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
package ch.xxx.manager.adapter.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

import org.apache.kafka.clients.DefaultHostResolver;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;


@Configuration
@EnableKafka
@Profile("kafka | prod-kafka")
public class KafkaConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfig.class);
	public static final String NEW_USER_TOPIC = "new-user-topic";
	public static final String NEW_USER_DLT_TOPIC = "new-user-topic-retry";
	public static final String USER_LOGOUT_TOPIC = "user-logout-topic";
	public static final String USER_LOGOUT_DLT_TOPIC = "user-logout-topic-retry";
	private static final String GZIP = "gzip";
	private static final String ZSTD = "zstd";

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;
	
	@PostConstruct
	public void init() {
		DefaultHostResolver.IP_ADDRESS = this.bootstrapServers.split(":")[0];
		LOGGER.info("Kafka Servername: {} Ip Address: {}", DefaultHostResolver.IP_ADDRESS, DefaultHostResolver.KAFKAAPP);
	}
	
	@Bean
	public ProducerFactory<String,String> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers); 
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        DefaultKafkaProducerFactory<String,String> defaultKafkaProducerFactory = new DefaultKafkaProducerFactory<>(configProps);
        defaultKafkaProducerFactory.setTransactionIdPrefix("tx-");        
        return defaultKafkaProducerFactory;
	}
	
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean("kafkaRetryTemplate")
    public KafkaTemplate<String, String> kafkaRetryTemplate() {
        KafkaTemplate<String,String> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        kafkaTemplate.setAllowNonTransactional(true);
        return kafkaTemplate;
    }
    
    @Bean
    public KafkaTransactionManager<String,String> kafkaTransactionManager() {
        KafkaTransactionManager<String,String> manager = new KafkaTransactionManager<>(producerFactory());
        return manager;
    }
       	    
    
	@Bean
	public NewTopic newUserTopic() {
		return TopicBuilder.name(KafkaConfig.NEW_USER_TOPIC).config(TopicConfig.COMPRESSION_TYPE_CONFIG, KafkaConfig.GZIP).compact().build();
	}
	
	@Bean
	public NewTopic userLogoutTopic() {
		return TopicBuilder.name(KafkaConfig.USER_LOGOUT_TOPIC).config(TopicConfig.COMPRESSION_TYPE_CONFIG, KafkaConfig.GZIP).compact().build();
	}

    @Bean
    @Primary
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

	@EventListener(ApplicationReadyEvent.class)
	public void doOnStartup() {
		// this.newUserTopic();
		// this.userLogoutTopic();
	}
}
