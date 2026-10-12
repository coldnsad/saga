package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.domain.entity.OrderHistoryEntity;
import org.example.orderservice.domain.repository.OrderHistoryEntityRepository;
import org.example.orderservice.domain.status.OrderStatus;
import org.example.orderservice.service.impl.OrderHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderHistoryEntityRepository historyEntityRepository;

    @Override
    public void addHistory(UUID orderId, OrderStatus status) {
        log.info("Add to history with id:{} status:{}", orderId, status.toString());
        OrderHistoryEntity orderHistoryEntity = OrderHistoryEntity.builder()
                .actionTime(LocalDateTime.now())
                .orderId(orderId)
                .status(status)
                .build();
        historyEntityRepository.save(orderHistoryEntity);
    }
}
