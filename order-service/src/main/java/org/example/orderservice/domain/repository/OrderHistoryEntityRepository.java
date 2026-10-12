package org.example.orderservice.domain.repository;

import org.example.orderservice.domain.entity.OrderHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderHistoryEntityRepository extends JpaRepository<OrderHistoryEntity, UUID> {
}
