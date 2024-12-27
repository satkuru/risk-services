package com.bank.risk.validation.bdd.steps;

import com.bank.risk.validation.bdd.config.SpringIntegrationTest;
import com.bank.risk.validation.trades.Trade;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ValidationStepDefinitions extends SpringIntegrationTest {
    @Autowired
    KafkaTemplate<String, Trade> kafkaTemplate;

    private final BlockingQueue<ConsumerRecord<String,Trade>> eligibleTradesQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ConsumerRecord<String,Trade>> tradeManagementQueue = new LinkedBlockingQueue<>();

    @Value("${topic.trades.incoming}")
    private String topic;

    @Value("${topic.trades.eligible}")
    private String eligibleTradesTopic;

    @Value("${topic.trades.management}")
    private String tradeManagementTopic;

    private Map<String,Trade> latestTradesReceived = new HashMap<>();

    @KafkaListener(topics = "eligibleTrades",groupId = "test-group-2")
    private void listenToEligibleTrades(ConsumerRecord<String, Trade> record) throws InterruptedException {
        log.info("Received an eligible trade message {}",record);
        eligibleTradesQueue.put(record);
    }

    @KafkaListener(topics = "tradeManagement",groupId = "test-group-3")
    private void listenToManagementTrades(ConsumerRecord<String, Trade> record) throws InterruptedException {
        log.info("Received trade management message {}",record);
        tradeManagementQueue.put(record);
    }

    @When("a trade message is received in valuation service")
    public void aTradeMessageWithAProductTypeFXOptionIsReceivedInValuationService(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class,String.class);
        for(Map<String,String> row: rows){
            Trade message = new Trade(row.get("reference"), row.get("productType"), row.get("account"), LocalDate.parse(row.get("maturityDate"), DateTimeFormatter.ISO_LOCAL_DATE), Long.valueOf(row.get("notional")));
            kafkaTemplate.send(topic,message);
            latestTradesReceived.put(message.ref(), message);
        }
    }

    @Then("the trade should be classified as eligible")
    public void theTradeShouldBeClassedAsEligible() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = eligibleTradesQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNotNull();
        Trade eligleTrade = singleRecord.value();
        Assertions.assertThat(eligleTrade).isNotNull();
        Assertions.assertThat(latestTradesReceived.get(eligleTrade.ref())).isEqualTo(eligleTrade);
    }

    @And("the trade message is send to trade control")
    public void theTradeMessageIsSendToTradeControl() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = tradeManagementQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNotNull();
        Trade sentToManagement = singleRecord.value();
        Assertions.assertThat(sentToManagement).isNotNull();
        Assertions.assertThat(latestTradesReceived.get(sentToManagement.ref())).isEqualTo(sentToManagement);

    }

    @Then("the trade should not be classified as eligible")
    public void theTradeShouldNotBeClassifiedAsEligible() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = eligibleTradesQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNull();
    }
}
