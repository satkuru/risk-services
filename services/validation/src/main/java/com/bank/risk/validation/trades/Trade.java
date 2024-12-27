package com.bank.risk.validation.trades;

public record Trade(String ref, String productType, String account, java.time.LocalDate maturityDate, Long notional) {
}
