package com.bank.risk.validation.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.retrytopic.*;

@Configuration
//@EnableKafkaRetryTopic
public class RetryConfiguration  extends RetryTopicConfigurationSupport {

//    @Bean
//    public RetryTopicConfiguration myRetryTopic(KafkaTemplate<String, Trade> template){
//        return RetryTopicConfigurationBuilder
//                .newInstance()
//                .fixedBackOff(3_000)
//                .maxAttempts(5)
//                .useSingleTopicForSameIntervals()
//                .create(template);
//    }

    @Override
    protected RetryTopicComponentFactory createComponentFactory(){
        return new RetryTopicComponentFactory(){
            @Override
            public RetryTopicNamesProviderFactory retryTopicNamesProviderFactory(){
                return new CustomRetryTopicNamesProviderFactory();
            }
        };
    }


    @Slf4j
    static
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
}
