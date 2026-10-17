package io.github.fathurrh.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
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
@EmbeddedKafka(partitions = 1, topics = {Topics.ORDERS, Topics.PAYMENTS})
class OrderFlowTest {

    @Autowired
    private OrderController controller;

    @Autowired
    private OrderStore store;

    @Autowired
    private KafkaTemplate<String, Object> kafka;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Test
    void publishesOrderCreatedAndUpdatesStatusFromPaymentResult() throws InterruptedException {
        Map<String, Object> props = KafkaTestUtils.consumerProps("test", "true", broker);
        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(), new StringDeserializer()).createConsumer()) {
            broker.consumeFromAnEmbeddedTopic(consumer, Topics.ORDERS);

            Order order = controller.create(
                    new OrderController.CreateOrderRequest("c-1", new BigDecimal("120"))).getBody();
            assertThat(order.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);

            var record = KafkaTestUtils.getSingleRecord(consumer, Topics.ORDERS, Duration.ofSeconds(15));
            assertThat(record.key()).isEqualTo(order.id());

            kafka.send(Topics.PAYMENTS, order.id(), new PaymentProcessedEvent(order.id(), true, "charged"));

            for (int i = 0; i < 100 && store.find(order.id()).orElseThrow().status() != OrderStatus.PAID; i++) {
                Thread.sleep(100);
            }
            assertThat(store.find(order.id()).orElseThrow().status()).isEqualTo(OrderStatus.PAID);
        }
    }
}
