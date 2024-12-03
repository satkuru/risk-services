package com.bank.risk.validation.steps;

import com.bank.risk.validation.service.ValidationService;
import com.bank.risk.validation.trades.Trade;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
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

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Value("${topic.name}")
    private String topic;

    @Value("${trade.eligible}")
    private String eligibleTradesTopic;

    @Value("${trade.management}")
    private String tradeManagementTopic;
    @Autowired
    private ValidationService validationService;

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

    private Trade latestTradeReceived;
    private Consumer<String, Trade> consumer;

    @Before
    public  void setupEnv(){
//        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", broker);
//        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        JsonDeserializer<Trade> deserializer = new JsonDeserializer<>(Trade.class);
//        deserializer.setRemoveTypeHeaders(false);
//        deserializer.addTrustedPackages("*");
//        deserializer.setUseTypeMapperForKey(true);
//        DefaultKafkaConsumerFactory<String, Trade> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps,new StringDeserializer(),deserializer);
//        consumer = consumerFactory.createConsumer();
//        broker.consumeFromEmbeddedTopics(consumer,eligibleTradesTopic,tradeManagementTopic);
    }

    @When("a trade message with a product type {string} is received in valuation service")
    public void aTradeMessageWithAProductTypeFXOptionIsReceivedInValuationService(String productType) {
        log.info("Sending trade message with product type {}",productType);
        Trade message = new Trade(1L,productType, "Abc123453se", LocalDate.of(2024,10,30),20_000_00L);
        kafkaTemplate.send(topic,message);
        latestTradeReceived = message;

    }

    @Then("the trade should be classified as eligible")
    public void theTradeShouldBeClassedAsEligible() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = eligibleTradesQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNotNull();
        Trade eligleTrade = singleRecord.value();
        Assertions.assertThat(eligleTrade).isNotNull();
        Assertions.assertThat(eligleTrade).isEqualTo(latestTradeReceived);

    }

    @And("the trade message is send to trade control")
    public void theTradeMessageIsSendToTradeControl() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = tradeManagementQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNotNull();
        Trade sentToManagement = singleRecord.value();
        Assertions.assertThat(sentToManagement).isNotNull();
        Assertions.assertThat(sentToManagement).isEqualTo(latestTradeReceived);
    }

    @Then("the trade should not be classified as eligible")
    public void theTradeShouldNotBeClassifiedAsEligible() throws InterruptedException {
        ConsumerRecord<String, Trade> singleRecord = eligibleTradesQueue.poll(3, TimeUnit.SECONDS);
        Assertions.assertThat(singleRecord).isNull();
    }
}
