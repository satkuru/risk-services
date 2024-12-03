package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EligibilityValidation {
    public boolean validate(Trade trade) {
        log.info("Validate trade for eligibility {}",trade);
        return "FX Option".equals(trade.productType());
    }
}
