package io.github.fathurrh.order;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class Topics {

    public static final String ORDERS = "orders";
    public static final String PAYMENTS = "payments";

    // 3 partitions so you can later run several consumers in one group and watch Kafka split the work.
    @Bean
    NewTopic ordersTopic() {
        return TopicBuilder.name(ORDERS).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic paymentsTopic() {
        return TopicBuilder.name(PAYMENTS).partitions(3).replicas(1).build();
    }
}
