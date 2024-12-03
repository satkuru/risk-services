package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeProcessor {
    private final EligibilityValidation validation;
    private final TradePublisher publisher;
    private final TradeManager manager;
    public void process(Trade trade) {
        boolean valid = validation.validate(trade);
        if(valid){
            log.info("publishing to eligible trade topic");
            String key = publisher.publish(trade);
        }
        log.info("publishing to trade management topic");
        manager.persist(trade);
    }
}
