package io.github.fathurrh.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

/** Runs against an in-memory Kafka broker, so no Docker is needed for tests. */
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {"orders", "payments"})
class PaymentFlowTest {

    @Autowired
    private KafkaTemplate<String, Object> kafka;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Test
    void publishesAPaymentResultForEveryOrder() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("test", "true", broker);
        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(), new StringDeserializer()).createConsumer()) {
            broker.consumeFromAnEmbeddedTopic(consumer, "payments");

            kafka.send("orders", "o-42",
                    new OrderCreatedEvent("o-42", "c-1", new java.math.BigDecimal("99.90")));

            var record = KafkaTestUtils.getSingleRecord(consumer, "payments", Duration.ofSeconds(15));
            assertThat(record.key()).isEqualTo("o-42");
            assertThat(record.value()).contains("\"success\":true");
        }
    }
}
