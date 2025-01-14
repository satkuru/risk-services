package com.bank.risk.validation.config;

import com.bank.risk.validation.trades.Trade;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {
    @Value("${topic.trades.input}")
    private String tradeTopic;
    @Bean
    public ProducerFactory<String, Trade> producerFactory(){
        return new DefaultKafkaProducerFactory<>(loadProducerConfigs());
    }

    @Bean
    public ConsumerFactory<String,Trade> consumerFactory(){
        return new DefaultKafkaConsumerFactory<>(loadConsumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Trade> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Trade> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(1);
        return factory;
    }
    private Map<String, Object> loadConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG,"test-group-1");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES,"*");
        return props;
    }

//    @Bean
//    public RetryTopicConfiguration myRetryTopic(KafkaTemplate<String, Trade> template) {
//        return RetryTopicConfigurationBuilder
//                .newInstance()
//                .maxAttempts(4)
//                .fixedBackOff(3000)
//                .includeTopic(tradeTopic)
//                .create(template);
//    }

    private Map<String, Object> loadProducerConfigs() {
        Map<String,Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public KafkaTemplate<String, Trade> kafkaTemplate() {
        return new KafkaTemplate<String, Trade>(producerFactory());
    }

}
