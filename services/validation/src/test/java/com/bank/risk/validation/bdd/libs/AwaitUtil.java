package com.bank.risk.validation.bdd.libs;

import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.Callable;

public class AwaitUtil {
    public static void wait(Callable<Boolean> condition){
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .with()
                .pollDelay(Duration.ZERO)
                .pollInterval(Duration.ofMillis(500))
                .until(condition);
    }
}
