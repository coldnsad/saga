package org.example.stockservice.service.handler;

import lombok.RequiredArgsConstructor;
import org.example.core.dto.commands.CancelReservationCommand;
import org.example.core.dto.commands.ProductReserveCommand;
import org.example.core.dto.events.CancelReservationSucceededEvent;
import org.example.core.dto.events.ProductReservedEvent;
import org.example.core.dto.exception.InsufficientProductQuantityException;
import org.example.stockservice.dto.ProductDto;
import org.example.stockservice.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@KafkaListener(topics = {
        "${stock.command.topic.name}"
})
@RequiredArgsConstructor
public class StockCommandsHandler {

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${stock.event.topic.name}")
    private String stockEventTopic;

    @KafkaHandler
    public void handleCommand(@Payload ProductReserveCommand productReserveCommand) {
        try {
            ProductDto reserveProduct = ProductDto.builder()
                    .id(productReserveCommand.productId())
                    .quantity(productReserveCommand.productQuantity())
                    .build();

            ProductDto reservedProduct = productService.reserveProduct(reserveProduct);
            ProductReservedEvent productReservedEvent = new ProductReservedEvent(
                    productReserveCommand.orderId(),
                    productReserveCommand.productId(),
                    productReserveCommand.productQuantity(),
                    reservedProduct.price()
            );
            kafkaTemplate.send(stockEventTopic, productReservedEvent);
        } catch (InsufficientProductQuantityException e) {
            CancelReservationSucceededEvent cancelEvent = new CancelReservationSucceededEvent(
                    productReserveCommand.orderId(),
                    productReserveCommand.productId()
            );
            kafkaTemplate.send(stockEventTopic, cancelEvent);
        }
    }

    @KafkaHandler
    public void handleCommand(@Payload CancelReservationCommand command) {
        ProductDto productToCancel = ProductDto.builder()
                .id(command.productId())
                .quantity(command.productQuantity())
                .build();
        try {
            productService.cancelReservation(productToCancel);
            CancelReservationSucceededEvent cancelEvent = new CancelReservationSucceededEvent(
                    command.orderId(),
                    command.productId()
            );
            kafkaTemplate.send(stockEventTopic, cancelEvent);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
