package com.bank.risk.validation.steps;

import com.bank.risk.validation.trades.Trade;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@ContextConfiguration
@EmbeddedKafka(partitions = 1, topics = {"tradeHistory","eligibleTrades","tradeManagement"})
@TestPropertySource(properties = "spring.kafka.consumer.auto-offset-reset = earliest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ValidationStepDefinitions {
    @Autowired
    KafkaTemplate<String, Trade> kafkaTemplate;

    private final BlockingQueue<ConsumerRecord<String,Trade>> eligibleTradesQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ConsumerRecord<String,Trade>> tradeManagementQueue = new LinkedBlockingQueue<>();

    @Value("${topic.name}")
    private String topic;

    @Value("${trade.eligible}")
    private String eligibleTradesTopic;

    @Value("${trade.management}")
    private String tradeManagementTopic;

    private Map<Long,Trade> latestTradesReceived = new HashMap<>();

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
            Trade message = new Trade(Long.valueOf(row.get("id")), row.get("productType"), row.get("account"), LocalDate.parse(row.get("maturityDate"), DateTimeFormatter.ISO_LOCAL_DATE), Long.valueOf(row.get("notional")));
            kafkaTemplate.send(topic,message);
            latestTradesReceived.put(message.id(), message);
        }
    }

    @Then("the trade should be classified as eligible")
    public void theTradeShouldBeClassedAsEligible() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = eligibleTradesQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNotNull();
        Trade eligleTrade = singleRecord.value();
        Assertions.assertThat(eligleTrade).isNotNull();
        Assertions.assertThat(latestTradesReceived.get(eligleTrade.id())).isEqualTo(eligleTrade);
    }

    @And("the trade message is send to trade control")
    public void theTradeMessageIsSendToTradeControl() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = tradeManagementQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNotNull();
        Trade sentToManagement = singleRecord.value();
        Assertions.assertThat(sentToManagement).isNotNull();
        Assertions.assertThat(latestTradesReceived.get(sentToManagement.id())).isEqualTo(sentToManagement);

    }

    @Then("the trade should not be classified as eligible")
    public void theTradeShouldNotBeClassifiedAsEligible() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = eligibleTradesQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNull();
    }
}
