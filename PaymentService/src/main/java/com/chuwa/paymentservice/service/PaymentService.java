package com.chuwa.paymentservice.service;

import com.chuwa.paymentservice.model.Payment;
import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentService {

    Payment processPayment(String orderId, BigDecimal amount);

    Optional<Payment> getPaymentByOrderId(String orderId);

    Payment submitPayment(String orderId);

    Payment reversePayment(String orderId);
}