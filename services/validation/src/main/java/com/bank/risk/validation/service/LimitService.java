package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@Component
@EnableRetry
public class LimitService {

    @Retryable(maxAttempts = 5, retryFor = {RuntimeException.class, HttpServerErrorException.class})
    public void checkLimit(Trade trade){

        //TODO: can call external service like REST endpoints
        try{
            log.info("Retrying attempt #:{}", RetrySynchronizationManager.getContext().getRetryCount());
            //can be call to an external service
            throw new RuntimeException("Check Trade Limit validation exception");
        } catch (RuntimeException e) {
            log.error("An exception was thrown while checking trade limit");
            throw e;
        }

    }

    @Recover
    private void handleErrors(RuntimeException exception, Trade trade){
        log.error("Trade limit checking service unavailable, trade = {}",trade,exception);
        throw  exception;
    }
}
