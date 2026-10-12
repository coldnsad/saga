package org.example.orderservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.commands.*;
import org.example.core.dto.events.*;
import org.example.orderservice.domain.status.OrderStatus;
import org.example.orderservice.service.impl.OrderHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(
        topics = {
                "${order.events.topic.name}",
                "${stock.event.topic.name}",
                "${payment.event.topic.name}"
        }
)
public class OrderOrchestrator {

    private final OrderHistoryService historyService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value(value = "${stock.command.topic.name}")
    private String stockCommandTopicName;
    @Value(value = "${payment.command.topic.name}")
    private String paymentCommandTopicName;
    @Value(value = "${order.command.topic.name}")
    private String orderCommandTopicName;

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent orderCreatedEvent) {
        ProductReserveCommand reserveProductCommand = new ProductReserveCommand(
                orderCreatedEvent.orderId(),
                orderCreatedEvent.productId(),
                orderCreatedEvent.productQuantity()
        );

        kafkaTemplate.send(stockCommandTopicName, reserveProductCommand);
        historyService.addHistory(orderCreatedEvent.orderId(), OrderStatus.CREATED);
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent productReservedEvent) {
        PaymentPayCommand paymentPayCommand = new PaymentPayCommand(
                productReservedEvent.orderId(),
                productReservedEvent.productId(),
                productReservedEvent.productQuantity(),
                productReservedEvent.price()
        );

        kafkaTemplate.send(paymentCommandTopicName, paymentPayCommand);
        historyService.addHistory(productReservedEvent.orderId(), OrderStatus.RESERVED);
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductPaidEvent productPaidEvent) {
        OrderApprovedCommand orderApprovedCommand = new OrderApprovedCommand(
                productPaidEvent.orderId(),
                productPaidEvent.productId()
        );
        kafkaTemplate.send(orderCommandTopicName, orderApprovedCommand);
        historyService.addHistory(productPaidEvent.orderId(), OrderStatus.PAID);
    }

    @KafkaHandler
    public void handleEvent(@Payload PaymentFailedEvent paymentFailedEvent) {
        CancelReservationCommand command = new CancelReservationCommand(
                paymentFailedEvent.orderId(),
                paymentFailedEvent.productId(),
                paymentFailedEvent.quantity()
        );
        kafkaTemplate.send(stockCommandTopicName, command);
    }

    @KafkaHandler
    public void handleEvent(@Payload CancelReservationSucceededEvent cancelEvent) {
        RejectOrderCommand command = new RejectOrderCommand(
                cancelEvent.orderId()
        );
        kafkaTemplate.send(orderCommandTopicName, command);
        historyService.addHistory(cancelEvent.orderId(), OrderStatus.REJECTED);
    }
}
