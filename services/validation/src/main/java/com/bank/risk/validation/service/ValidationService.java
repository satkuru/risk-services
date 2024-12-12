package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {
    private final TradeProcessor processor;

    @KafkaListener(topics = "${topic.trades.incoming}", groupId = "${group.id}")
    public void process(Trade trade){
        log.info("trade message received {}",trade);
        processor.process(trade);
    }

    public void listen(ConsumerRecord<String, GenericRecord> record){
        String key = record.key();
        String product = (String) record.value().get("product");
    }
}
