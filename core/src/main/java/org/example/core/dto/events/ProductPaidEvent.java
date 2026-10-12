package org.example.core.dto.events;

import java.math.BigDecimal;
import java.util.UUID;


public record ProductPaidEvent(
        UUID orderId,
        UUID productId,
        BigDecimal price
) { }
