package org.example.orderservice.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record RequestCreateOrderDto(
        UUID productId,
        Integer productQuantity
) {}
