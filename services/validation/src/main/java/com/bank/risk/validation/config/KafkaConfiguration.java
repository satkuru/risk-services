package com.bank.risk.validation.config;

import com.bank.risk.validation.trades.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfiguration {
    @Value("${topic.trades.input}")
    private String tradeTopic;
    @Bean
    public ProducerFactory<String, Trade> producerFactory(){
        return new DefaultKafkaProducerFactory<>(loadProducerConfigs());
    }



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


    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return new KafkaAdmin(configs);
    }

    //Create the required topics beforehand
    @Bean
    public KafkaAdmin.NewTopics tradeTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name("tradeInput")
                        .partitions(3)
                        .build(),
                TopicBuilder.name("tradeInput-retry")
                        .partitions(3)
                        .build(),
                TopicBuilder.name("tradeInput-dlt")
                        .partitions(3)
                        .build(),
                TopicBuilder.name("tradeHistory")
                        .replicas(1)
                        .build(),
                TopicBuilder.name("eligibleTrades")
                        .partitions(3)
                        .build(),
                TopicBuilder.name("tradeManagement")
                        .partitions(3)
                        .build());
    }

}

