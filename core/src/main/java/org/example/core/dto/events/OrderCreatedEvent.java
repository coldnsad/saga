package org.example.core.dto.events;

import java.util.UUID;


public record OrderCreatedEvent(
        UUID orderId,
        UUID productId,
        Integer productQuantity
) {
}
