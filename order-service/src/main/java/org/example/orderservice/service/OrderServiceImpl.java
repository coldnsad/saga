package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.events.OrderCreatedEvent;
import org.example.orderservice.domain.entity.OrderEntity;
import org.example.orderservice.domain.repository.OrderEntityRepository;
import org.example.orderservice.domain.status.OrderStatus;
import org.example.orderservice.dto.RequestCreateOrderDto;
import org.example.orderservice.service.impl.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderEntityRepository orderEntityRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    @Value("${order.events.topic.name}")
    private String orderEventTopicName;

    @Override
    public String createOrder(RequestCreateOrderDto createOrderDto) {
        OrderEntity orderEntity = OrderEntity.builder()
                .productId(createOrderDto.productId())
                .productQuantity(createOrderDto.productQuantity())
                .status(OrderStatus.CREATED)
                .build();
        orderEntityRepository.save(orderEntity);
        log.info("Order created...");

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                orderEntity.getId(),
                orderEntity.getProductId(),
                orderEntity.getProductQuantity()
        );
        log.info("Sending to topic: {}", orderEventTopicName);

        kafkaTemplate.send(orderEventTopicName, orderCreatedEvent);

        return orderEntity.getId().toString();
    }

    @Override
    public void approveOrder(UUID orderId) {
        OrderEntity orderEntity = orderEntityRepository.findById(orderId).orElseThrow();
        orderEntity.setStatus(OrderStatus.APPROVED);
        orderEntityRepository.save(orderEntity);
        log.info("Saved with id: {} and Status: {}", orderId, OrderStatus.APPROVED);
    }

    @Override
    public void rejectOrder(UUID orderId) {
        OrderEntity orderEntity = orderEntityRepository.findById(orderId).orElseThrow();
        orderEntity.setStatus(OrderStatus.REJECTED);
        orderEntityRepository.save(orderEntity);
        log.info("Saved with id: {} and Status: {}", orderId, OrderStatus.REJECTED);
    }
}
