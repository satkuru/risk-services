package com.bank.risk.validation.model;

import java.time.LocalDate;

public record TradeDto(String tradeRef, String productType, String account, LocalDate maturity, Long notionalAmount) {
}
