package org.example.core.dto.events;

import java.util.UUID;


public record CancelReservationSucceededEvent(
        UUID orderId,
        UUID productId
) {
}
