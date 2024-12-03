package com.bank.risk.validation.steps;

import com.bank.risk.validation.service.ValidationService;
import com.bank.risk.validation.trades.Trade;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.time.LocalDate;

@Slf4j
public class ValidationStepDefinitions {
    @Autowired
    KafkaTemplate<String, Trade> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Value("${topic.name}")
    private String topic;

    @Value("${trade.eligible}")
    private String topicEligible;

    @Autowired
    private ValidationService validationService;

    private Trade latestTradeReceived;

    @Given("trade message with id  {long} is received")
    public void tradeMessageWithIdIsReceived(long id) {
        log.info("Sending trade message with id{}",id);
        Trade message = new Trade(id,"FX Option", "Abc123453se", LocalDate.of(2024,10,30),20_000_00L);
        kafkaTemplate.send(topic,message);
        latestTradeReceived = message;

    }

    @When("the trade message is a product type of {string}")
    public void theTradeMessageIsAProductTypeOfFXOption( String productType) {
        log.info("Received trade message of type {}",productType);
        Assertions.assertThat(productType.equals(latestTradeReceived.productType()));
    }

    @Then("the trade should be classed as eligible")
    public void theTradeShouldBeClassedAsEligible() {

    }

    @And("the trade message is send to trade control")
    public void theTradeMessageIsSendToTradeControl() {
    }


}
