package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EligibilityValidation {
    @Value("${trade.eligible.products}")
    private List<String> eligibleProducts;

    public boolean validate(Trade trade) {
        log.info("Validate trade for eligibility {}",trade);
        return eligibleProducts.contains(trade.productType());
    }
}
