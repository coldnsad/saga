package org.example.stockservice.domain.repository;

import org.example.stockservice.domain.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductEntityRepository extends JpaRepository<ProductEntity, UUID> {
}
