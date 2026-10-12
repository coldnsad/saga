package org.example.core.dto.commands;

import java.util.UUID;


public record RejectOrderCommand(
        UUID orderId
) {
}
