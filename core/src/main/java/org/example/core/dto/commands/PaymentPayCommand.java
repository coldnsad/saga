package org.example.core.dto.commands;

import java.math.BigDecimal;
import java.util.UUID;


public record PaymentPayCommand(
        UUID orderId,
        UUID productId,
        Integer quantity,
        BigDecimal price
) { }
