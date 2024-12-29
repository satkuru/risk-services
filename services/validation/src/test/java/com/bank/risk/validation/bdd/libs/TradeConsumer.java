package com.bank.risk.validation.bdd.libs;

import com.bank.risk.validation.trades.Trade;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.util.Objects;

@Slf4j
public class TradeConsumer {

    @Value("${topic.trades.eligible}")
    private String eligibleTradesTopic;

    @Autowired
    private EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    private Consumer<String, Trade> tradeConsumer;

    @Before(value = "@TradeEligibility")
    public void createTradeConsumer(){
        log.info("Create Trade Consumer for topic {}",eligibleTradesTopic);
        tradeConsumer = KafkaConsumerUtil.setupConsumer(kafkaBroker,new StringDeserializer(),new JsonDeserializer<>(Trade.class, objectMapper));
        kafkaBroker.consumeFromAnEmbeddedTopic(tradeConsumer,eligibleTradesTopic);
    }

    public Consumer<String, Trade> getTradeConsumer(){
        if(Objects.isNull(tradeConsumer)){
            throw new RuntimeException("No trade consumer created");
        }
        return tradeConsumer;
    }

    @After(value = "@TradeEligibility")
    public void closeConsumer(){
        log.info("Close Trade Consumer for topic {}",eligibleTradesTopic);
        this.tradeConsumer.close();
    }

}
