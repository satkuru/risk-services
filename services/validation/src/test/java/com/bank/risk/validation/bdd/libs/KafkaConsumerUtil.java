package com.bank.risk.validation.bdd.libs;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class KafkaConsumerUtil {

    public static <K,V> Consumer<K, V> setupConsumer(final EmbeddedKafkaBroker kafkaBroker,
                                                     final Deserializer<K> keyDeserializer,
                                                     final Deserializer<V> valueDeserializer){
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", kafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        DefaultKafkaConsumerFactory<K, V> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps, keyDeserializer, valueDeserializer);
        return consumerFactory.createConsumer();
    }

    public static <K, V> ConsumerRecord<K, V> consume(Consumer<K, V> consumer, String topic, final Predicate<? super ConsumerRecord<K, V>> condition){
        final AtomicReference<ConsumerRecord<K, V>> dataReceived = new AtomicReference<>();
        AwaitUtil.wait(()->{
            Iterable<ConsumerRecord<K, V>> records = KafkaTestUtils.getRecords(consumer).records(topic);
            ConsumerRecord<K, V> record = StreamSupport.stream(records.spliterator(), false)
                    .filter(condition)
                    .findFirst()
                    .orElse(null);
            dataReceived.set(record);
            return record!=null;

        });
        return dataReceived.get();
    }

    public static <K, V> ConsumerRecord<K, V>  consume(K id, Consumer<K, V> consumer, String topic){
        return consume(consumer,topic,kv->kv.value().equals(id));
    }

}
