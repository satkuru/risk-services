package com.bank.risk.validation.steps;

import com.bank.risk.validation.ValidationApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ValidationApplication.class)
@CucumberContextConfiguration
@EmbeddedKafka(topics = {"${topic.trades.eligible}","${topic.trades.management}","${topic.trades.incoming}"})
@DirtiesContext
@TestPropertySource(locations="classpath:application.yml")
public class SpringIntegrationTest {
}
