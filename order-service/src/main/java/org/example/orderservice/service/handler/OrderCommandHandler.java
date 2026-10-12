package org.example.orderservice.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.commands.OrderApprovedCommand;
import org.example.core.dto.commands.RejectOrderCommand;
import org.example.orderservice.service.impl.OrderService;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = {
        "${order.command.topic.name}"
})
public class OrderCommandHandler {

    private final OrderService orderService;

    @KafkaHandler
    public void handleCommand(@Payload OrderApprovedCommand command){
        orderService.approveOrder(command.orderId());
    }

    @KafkaHandler
    public void handleCommand(@Payload RejectOrderCommand command){
        orderService.rejectOrder(command.orderId());
    }
}
