package org.example.stockservice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductDto(
        UUID id,
        String productName,
        Integer quantity,
        BigDecimal price
) {
}
