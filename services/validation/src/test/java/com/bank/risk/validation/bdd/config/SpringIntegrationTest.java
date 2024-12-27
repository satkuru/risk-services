package com.bank.risk.validation.bdd.config;

import com.bank.risk.validation.ValidationApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;


@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ValidationApplication.class)
@CucumberContextConfiguration
@EmbeddedKafka( partitions = 1,
        topics = {"${topic.trades.eligible}","${topic.trades.management}","${topic.trades.incoming}"},
        controlledShutdown = true)
@DirtiesContext
public class SpringIntegrationTest {

}
