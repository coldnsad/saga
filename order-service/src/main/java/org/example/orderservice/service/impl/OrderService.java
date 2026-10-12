package org.example.orderservice.service.impl;

import org.example.orderservice.dto.RequestCreateOrderDto;

import java.util.UUID;

public interface OrderService {
    String createOrder(RequestCreateOrderDto createOrderDto);
    void approveOrder(UUID orderId);
    void rejectOrder(UUID orderId);
}
