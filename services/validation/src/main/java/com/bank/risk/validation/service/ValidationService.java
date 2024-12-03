package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {
    private final TradeProcessor processor;

    @KafkaListener(topics = "${topic.name}", groupId = "${group.id}")
    public void process(Trade trade){
        log.info("trade message received {}",trade);
        processor.process(trade);
    }
}
