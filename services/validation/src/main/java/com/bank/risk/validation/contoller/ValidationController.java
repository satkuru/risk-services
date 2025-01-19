package com.bank.risk.validation.contoller;

import com.bank.risk.validation.model.TradeDto;
import com.bank.risk.validation.trades.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class ValidationController {

    @Value("${topic.trades.input}")
    private String tradeTopic;

    private final KafkaTemplate<String,Trade> kafkaTemplate;
    @PostMapping
    public ResponseEntity<String> create(@RequestBody TradeDto payload){
        log.info("Sending trade to topic:"+tradeTopic);
        Trade trade =  toTrade(payload);
        CompletableFuture<SendResult<String, Trade>> future = kafkaTemplate.send(tradeTopic,trade);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                handleSuccess(payload);
            }
            else {
                handleFailure(payload, trade, ex);
            }
        });
        return new ResponseEntity<>(payload.tradeRef(), HttpStatus.CREATED);
    }

    private void handleFailure(TradeDto payload, Trade record, Throwable ex) {
        log.error("Failed to publish the trade request: payload={}, record={}, exception={}",payload,record,ex.getMessage());
    }

    private void handleSuccess(TradeDto payload) {
        log.info("Received trade request send to topic successfully :"+payload);
    }

    private Trade toTrade(TradeDto payload) {
        return new Trade(payload.tradeRef(),
                payload.productType(),
                payload.account(),
                payload.maturity(),
                payload.notionalAmount());
    }


}
