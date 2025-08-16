package com.chuwa.paymentservice.service;

import com.chuwa.paymentservice.model.Payment;
import com.chuwa.paymentservice.model.PaymentStatus;
import com.chuwa.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public Payment processPayment(String orderId, BigDecimal amount) {
        // ... existing idempotency logic ...
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        Payment newPayment = new Payment();
        newPayment.setOrderId(orderId);
        newPayment.setAmount(amount);
        newPayment.setStatus(PaymentStatus.PENDING);
        newPayment.setCreatedAt(Instant.now());

        return paymentRepository.save(newPayment);
    }

    @Override
    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public Payment submitPayment(String orderId) {
        // Find the PENDING payment record
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for order: " + orderId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING state.");
        }

        // Simulate calling an external payment gateway and getting a successful response
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setUpdatedAt(Instant.now());
        Payment savedPayment = paymentRepository.save(payment);

        // Publish the payment result to a Kafka topic
        kafkaTemplate.send("payment-processed-topic", "Payment COMPLETED for order: " + orderId);

        return savedPayment;
    }

    @Override
    public Payment reversePayment(String orderId) {
        // 1. Find the payment or throw an exception
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for order: " + orderId));

        // 2. Business Rule: Only completed payments can be refunded
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot refund a payment that is not in COMPLETED state.");
        }

        // 3. Update status and save
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(Instant.now());
        Payment refundedPayment = paymentRepository.save(payment);

        // 4. Publish a refund event to Kafka
        kafkaTemplate.send("payment-refunded-topic", "Payment REFUNDED for order: " + orderId);

        return refundedPayment;
    }
}