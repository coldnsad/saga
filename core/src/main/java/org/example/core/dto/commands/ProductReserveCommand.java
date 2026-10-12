package org.example.core.dto.commands;

import java.util.UUID;


public record ProductReserveCommand(
        UUID orderId,
        UUID productId,
        Integer productQuantity
) {
}
