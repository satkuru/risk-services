package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {
    private final TradeProcessor processor;
    private final LimitService limitService;

    @RetryableTopic(
            attempts = "2",
            backoff = @Backoff(delay = 1000),
            autoCreateTopics = "false",
            include = {RuntimeException.class}
    )
    @KafkaListener(topics = "${topic.trades.incoming}", groupId = "${group.id}")
    public void process(Trade trade){
        log.info("trade message received {}",trade);
        limitService.checkLimit(trade);
        processor.process(trade);
    }

    public void listen(ConsumerRecord<String, GenericRecord> record){
        String key = record.key();
        Trade trade = toTrade(record.value());
        processor.process(trade);
    }

    private Trade toTrade(GenericRecord record){
        String reference = (String) record.get("reference");
        String product = (String) record.get("product");
        String account = (String) record.get("account");
        String maturityStr = (String) record.get("maturity");
        LocalDate maturityDate = LocalDate.parse(maturityStr);
        Long notional = (Long) record.get("notional");
        return new Trade(reference,product,account,maturityDate,notional);
    }
}
