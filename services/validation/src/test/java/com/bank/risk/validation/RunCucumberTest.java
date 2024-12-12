package com.bank.risk.validation;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/",
        glue = {"com.bank.risk.demo.steps","com.bank.risk.validation.config"},
        plugin = {"pretty","html:target/cucumber-reports.html"}
)
public class RunCucumberTest {
}
