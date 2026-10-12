package org.example.core.dto.events;

import java.math.BigDecimal;
import java.util.UUID;


public record PaymentFailedEvent(
        UUID orderId,
        UUID productId,
        Integer quantity,
        BigDecimal price
) {
}
