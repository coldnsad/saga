package org.example.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaConfig {

    @Value("${order.events.topic.name}")
    private String orderEventTopicName;
    @Value("${stock.command.topic.name}")
    private String stockCommandTopicName;
    @Value("${stock.event.topic.name}")
    private String stockEventTopicName;
    @Value("${payment.event.topic.name}")
    private String paymentEventTopicName;
    @Value("${payment.command.topic.name}")
    private String paymentCommandTopicName;
    @Value("${order.command.topic.name}")
    private String orderCommandTopicName;

    @Bean
    KafkaAdmin.NewTopics createTopic() {
        return new KafkaAdmin.NewTopics(
                //Events
                TopicBuilder.name(orderEventTopicName).partitions(3).replicas(1).build(),
                TopicBuilder.name(stockEventTopicName).partitions(3).replicas(1).build(),
                TopicBuilder.name(paymentEventTopicName).partitions(3).replicas(1).build(),

                //Commands from saga
                TopicBuilder.name(stockCommandTopicName).partitions(3).replicas(1).build(),
                TopicBuilder.name(paymentCommandTopicName).partitions(3).replicas(1).build(),
                TopicBuilder.name(orderCommandTopicName).partitions(3).replicas(1).build()
        );
    }
}
