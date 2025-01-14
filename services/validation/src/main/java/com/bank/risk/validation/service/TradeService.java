package com.bank.risk.validation.service;


import com.bank.risk.validation.trades.Trade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TradeService {

    @DltHandler
    public void handleDLTMessage(Trade trade) {

        log.info("Trade with reference {} is rejected",trade);
        // ... message processing, persistence, etc
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 5000),
            autoCreateTopics = "true",
            dltTopicSuffix = ".DLT")
    @KafkaListener(topics = "${topic.trades.input}",containerFactory = "kafkaListenerContainerFactory")
    public void process(@Payload Trade trade){
        log.info("Trade received for processing: {} ",trade);
        throw new RuntimeException("trade cannot be processed"+trade.ref());
    }
}
