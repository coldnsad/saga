package org.example.orderservice.service.impl;

import org.example.orderservice.domain.status.OrderStatus;

import java.util.UUID;

public interface OrderHistoryService {
    void addHistory(UUID orderId, OrderStatus status);
}
