package com.bank.risk.validation.service;

import com.bank.risk.validation.trades.Trade;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.LocalDate;
import java.util.Map;


@EmbeddedKafka(topics = {"tradeHistory","eligibleTrades","tradeManagement"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers",
        partitions = 3)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class ValidationServiceTest {

    @Autowired
    KafkaTemplate<String, Trade> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Value("${topic.trades.incoming}")
    private String topicIncoming;

    @Value("${topic.trades.eligible}")
    private String topicEligible;

    @Value("${topic.trades.management}")
    private String topicManager;

    @Autowired
    private ValidationService service;

    private Consumer<String, Trade> eligigleTradesConsumer;
    private Consumer<String, Trade> tradeManagerConsumer;

    @BeforeEach
    void setUp() {
        Consumer<String,Trade> managementTrades;
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("group_consumer_test", "false", broker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        var consumerFactory = new DefaultKafkaConsumerFactory<String, Trade>(consumerProps, new StringDeserializer(), new JsonDeserializer<>(Trade.class, false));
        eligigleTradesConsumer = consumerFactory.createConsumer("eligibleTrades",null);
        tradeManagerConsumer = consumerFactory.createConsumer("tradeManagement",null);
        broker.consumeFromAnEmbeddedTopic(eligigleTradesConsumer,topicEligible);
        broker.consumeFromAnEmbeddedTopic(tradeManagerConsumer,topicManager);

    }

    @Test
    void processEligibleTrade() {
        Trade message = new Trade(1L, "FX Option", "abcs1234d", LocalDate.of(2024,11,29) , 10_000_000L);
        kafkaTemplate.send(topicIncoming,message);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ConsumerRecord<String, Trade> singleRecord = KafkaTestUtils.getSingleRecord(eligigleTradesConsumer, topicEligible);
        Trade sendTrade = singleRecord.value();
        Assertions.assertNotNull(sendTrade);
        Assertions.assertEquals(message,sendTrade);

        singleRecord = KafkaTestUtils.getSingleRecord(tradeManagerConsumer, topicManager);
        Trade savedTrade = singleRecord.value();
        Assertions.assertNotNull(savedTrade);
        Assertions.assertEquals(message,savedTrade);

    }
}