package org.example.core.dto.events;

import java.math.BigDecimal;
import java.util.UUID;


public record ProductReservedEvent(
        UUID orderId,
        UUID productId,
        Integer productQuantity,
        BigDecimal price
) {
}
