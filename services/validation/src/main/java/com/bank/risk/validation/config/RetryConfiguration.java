package com.bank.risk.validation.config;

import com.bank.risk.validation.trades.Trade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.*;

@Configuration
//@EnableKafkaRetryTopic
public class RetryConfiguration  extends RetryTopicConfigurationSupport {

    @Bean
    public RetryTopicConfiguration myRetryTopic(KafkaTemplate<String, Trade> template){
        return RetryTopicConfigurationBuilder
                .newInstance()
                .fixedBackOff(3_000)
                .maxAttempts(5)
                .useSingleTopicForSameIntervals()
                .create(template);
    }

    @Override
    protected RetryTopicComponentFactory createComponentFactory(){
        return new RetryTopicComponentFactory(){
            @Override
            public RetryTopicNamesProviderFactory retryTopicNamesProviderFactory(){
                return new CustomRetryTopicNamesProviderFactory();
            }
        };
    }
}
