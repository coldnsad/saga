package org.example.core.dto.commands;

import java.util.UUID;


public record CancelReservationCommand(
        UUID orderId,
        UUID productId,
        Integer productQuantity
) {
}
