package org.example.paymentservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalMockPaymentServiceImpl implements PaymentService {
    @Override
    public void pay() {
        log.info("Calling external payment service...");
        throw new RuntimeException("Недостаточно средств");
    }
}
