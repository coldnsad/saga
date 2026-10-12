package org.example.core.dto.commands;

import java.util.UUID;


public record OrderApprovedCommand(
        UUID orderId,
        UUID productId
) {
}
