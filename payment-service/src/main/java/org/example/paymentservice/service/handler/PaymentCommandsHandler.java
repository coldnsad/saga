package org.example.paymentservice.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.commands.PaymentPayCommand;
import org.example.core.dto.events.PaymentFailedEvent;
import org.example.core.dto.events.ProductPaidEvent;
import org.example.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@KafkaListener(topics = {
        "${payment.command.topic.name}"
})
@Slf4j
public class PaymentCommandsHandler {

    private final PaymentService paymentService;
    @Value(value = "${payment.event.topic.name}")
    private String paymentEventTopicName;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaHandler
    public void handleCommand(@Payload PaymentPayCommand payCommand){
        try {
            ProductPaidEvent productPaidEvent = new ProductPaidEvent(
                    payCommand.orderId(),
                    payCommand.productId(),
                    payCommand.price()
            );
            paymentService.pay();
            kafkaTemplate.send(paymentEventTopicName, productPaidEvent);
        } catch (RuntimeException e) {
            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(
                    payCommand.orderId(),
                    payCommand.productId(),
                    payCommand.quantity(),
                    payCommand.price()
            );
            log.info("Payment failed...");
            kafkaTemplate.send(paymentEventTopicName, paymentFailedEvent);
        }
    }
}
