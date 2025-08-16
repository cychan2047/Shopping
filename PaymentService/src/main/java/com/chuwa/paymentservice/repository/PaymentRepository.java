package com.chuwa.paymentservice.repository;

import com.chuwa.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds a payment by its associated order ID.
     * This method is key to ensuring idempotency.
     * @param orderId The unique ID of the order.
     * @return An Optional containing the payment if it exists.
     */
    Optional<Payment> findByOrderId(String orderId);
}