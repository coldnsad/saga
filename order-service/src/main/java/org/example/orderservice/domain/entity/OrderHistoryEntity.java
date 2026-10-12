package org.example.orderservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.orderservice.domain.status.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class OrderHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID orderId;
    private LocalDateTime actionTime;

    @Enumerated(value = EnumType.STRING)
    OrderStatus status;
}
