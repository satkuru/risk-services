package com.bank.risk.validation.bdd.steps;

import com.bank.risk.validation.bdd.config.SpringIntegrationTest;
import com.bank.risk.validation.bdd.libs.KafkaConsumerUtil;
import com.bank.risk.validation.bdd.libs.TradeConsumer;
import com.bank.risk.validation.trades.Trade;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Slf4j
public class ValidationStepDefinitions extends SpringIntegrationTest {
    @Autowired
    KafkaTemplate<String, Trade> kafkaTemplate;

    @Autowired
    private TradeConsumer tradeConsumer;

    private final BlockingQueue<ConsumerRecord<String,Trade>> eligibleTradesQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ConsumerRecord<String,Trade>> tradeManagementQueue = new LinkedBlockingQueue<>();

    @Value("${topic.trades.incoming}")
    private String topic;

    @Value("${topic.trades.eligible}")
    private String eligibleTradesTopic;

    @Value("${topic.trades.management}")
    private String tradeManagementTopic;

    private Map<String,Trade> latestTradesReceived = new HashMap<>();

    private Schema schema;

    @Before
    private void createSchema() throws IOException {
        log.info("Create Avro Schema object=========");
        schema = new Schema.Parser().parse(new File("src/main/avro/PTSTrade.avsc"));
    }

//    @KafkaListener(topics = "eligibleTrades",groupId = "test-group-2")
//    private void listenToEligibleTrades(ConsumerRecord<String, Trade> record) throws InterruptedException {
//        log.info("Received an eligible trade message {}",record);
//        eligibleTradesQueue.put(record);
//    }

    @KafkaListener(topics = "tradeManagement",groupId = "test-group-3")
    private void listenToManagementTrades(ConsumerRecord<String, Trade> record) throws InterruptedException {
        log.info("Received trade management message {}",record);
        tradeManagementQueue.put(record);
    }

    @When("a trade message is received in valuation service")
    public void aTradeMessageWithAProductTypeFXOptionIsReceivedInValuationService(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class,String.class);
        for(Map<String,String> row: rows){
            Trade trade = new Trade(row.get("reference").trim(), row.get("productType"), row.get("account"), LocalDate.parse(row.get("maturityDate"), DateTimeFormatter.ISO_LOCAL_DATE), Long.valueOf(row.get("notional")));
            Message<Trade> message = MessageBuilder
                    .withPayload(trade)
                    .setHeader(KafkaHeaders.KEY, trade.ref())
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.GROUP_ID,"test-group-1")
                    .build();
            //GenericRecord record = new GenericData.Record();
            kafkaTemplate.send(message);
            latestTradesReceived.put(trade.ref(),trade);
        }
    }

    @Then("the trade with reference {string} should be classified as eligible")
    public void theTradeShouldBeClassedAsEligible(String tradeReference) throws InterruptedException {
        Consumer<String, Trade> consumer = tradeConsumer.getTradeConsumer();
        Predicate<ConsumerRecord<String,Trade>> findTrade = r->r.value().ref().equals(tradeReference);
        ConsumerRecord<String, Trade> tradeRecord = KafkaConsumerUtil.consume(consumer,eligibleTradesTopic,findTrade);
        //ConsumerRecord<String, Trade> singleRecord = eligibleTradesQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(tradeRecord).isNotNull();
        Trade eligleTrade = tradeRecord.value();
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

    @Then("the trade with reference {string} should not be classified as eligible")
    public void theTradeShouldNotBeClassifiedAsEligible(String tradeReference) throws InterruptedException {
        Consumer<String, Trade> consumer = tradeConsumer.getTradeConsumer();
        Iterable<ConsumerRecord<String, Trade>> records = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(1000)).records(topic);
        Assertions.assertThat(records).isEmpty();
    }
}
