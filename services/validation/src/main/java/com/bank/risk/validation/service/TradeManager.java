package com.bank.risk.validation.service;


import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TradeManager {
    private final KafkaTemplate<String, Trade> kafkaTemplate;

    @Value("${topic.trades.management}")
    private String topic;

    public void persist(Trade trade){
        var uuid =  UUID.randomUUID().toString();
        kafkaTemplate.send(topic,uuid,trade);
    }
}
