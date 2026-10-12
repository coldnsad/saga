package org.example.orderservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.orderservice.domain.status.OrderStatus;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID productId;
    private Integer productQuantity;

    @Enumerated(value = EnumType.STRING)
    OrderStatus status;
}
