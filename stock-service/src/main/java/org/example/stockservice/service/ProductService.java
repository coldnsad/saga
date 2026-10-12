package org.example.stockservice.service;

import org.example.stockservice.dto.ProductDto;

public interface ProductService {
    ProductDto reserveProduct(ProductDto desiredProduct);

    void cancelReservation(ProductDto productToCancel);
}
