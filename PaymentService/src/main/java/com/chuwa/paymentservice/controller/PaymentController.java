package com.chuwa.paymentservice.controller;

import com.chuwa.paymentservice.model.Payment;
import com.chuwa.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Looks up a payment's status by its order ID.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId) {
        return paymentService.getPaymentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Submits a payment for processing.
     */
    @PostMapping("/{orderId}/submit")
    public ResponseEntity<Payment> submitPayment(@PathVariable String orderId) {
        try {
            Payment processedPayment = paymentService.submitPayment(orderId);
            return ResponseEntity.ok(processedPayment);
        } catch (Exception e) {
            // In a real app, handle exceptions more gracefully
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Payment> refundPayment(@PathVariable String orderId) {
        try {
            Payment refundedPayment = paymentService.reversePayment(orderId);
            return ResponseEntity.ok(refundedPayment);
        } catch (Exception e) {
            // Handle exceptions like payment not found or invalid state
            return ResponseEntity.badRequest().build();
        }
    }
}