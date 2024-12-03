package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeProcessor {
    private final EligibilityValidation validation;
    private final TradePublisher publisher;
    private final TradeManager manager;
    public void process(Trade trade) {
        boolean valid = validation.validate(trade);
        if(valid){
            String key = publisher.publish(trade);
        }
        manager.persist(trade);
    }
}
