package org.example.stockservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.dto.exception.InsufficientProductQuantityException;
import org.example.stockservice.domain.entity.ProductEntity;
import org.example.stockservice.domain.repository.ProductEntityRepository;
import org.example.stockservice.dto.ProductDto;
import org.example.stockservice.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductEntityRepository productEntityRepository;

    @Override
    public ProductDto reserveProduct(ProductDto desiredProduct) {
        ProductEntity productEntity = productEntityRepository.findById(desiredProduct.id()).orElseThrow();
        log.info("Product with id found: {}", productEntity.getId());
        if (productEntity.getQuantity() < desiredProduct.quantity()) {
            throw new InsufficientProductQuantityException("Товара на складе недостаточно");
        }

        productEntity.setQuantity(productEntity.getQuantity() - desiredProduct.quantity());
        productEntityRepository.save(productEntity);
        log.info("Reserved: {}, New quantity: {}", desiredProduct.quantity(), productEntity.getQuantity());
        return ProductDto.builder()
                .id(productEntity.getId())
                .productName(productEntity.getProductName())
                .quantity(desiredProduct.quantity())
                .price(BigDecimal.valueOf(desiredProduct.quantity() * 5L))
                .build();
    }

    @Override
    public void cancelReservation(ProductDto productToCancel) {

    }
}
