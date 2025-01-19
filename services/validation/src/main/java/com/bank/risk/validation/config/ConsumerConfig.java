package com.bank.risk.validation.config;

import com.bank.risk.validation.trades.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.retrytopic.DestinationTopic;
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory;
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

//@EnableKafkaRetryTopic
@Configuration
@Slf4j
public class ConsumerConfig {

    @Bean
    public ConsumerFactory<String, Trade> consumerFactory(){
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
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG,"test-group-1");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES,"*");
        return props;
    }
}

@Slf4j
class CustomRetryTopicNamesProviderFactory implements RetryTopicNamesProviderFactory {

    @Override
    public RetryTopicNamesProvider createRetryTopicNamesProvider(DestinationTopic.Properties properties) {

        if (properties.isDltTopic()) {
            return new SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider(properties) {
                @Override
                public String getTopicName(String topic) {
                    return topic+"-dlt";
                }
            };
        } else if (properties.isRetryTopic()){
            return new SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider(properties) {
                @Override
                public String getTopicName(String topic) {
                    return topic+"-retry";
                }
            };
        }else {
            return new SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider(properties);
        }
    }

}
