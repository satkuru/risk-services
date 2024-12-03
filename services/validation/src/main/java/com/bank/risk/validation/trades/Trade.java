package com.bank.risk.validation.trades;

public record Trade(Long id, String productType, String account, java.time.LocalDate maturityDate, Long notional) {
}
