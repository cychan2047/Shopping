package com.chuwa.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-created-topic", groupId = "payment-group")
    public void handleOrderCreated(String message) {
        System.out.println("Received Order Created Event: " + message);

        // This is a simple parser. A real application would use a structured
        // format like JSON for Kafka messages.
        try {
            // Example message: "Order created with ID: uuid, Total Amount: 123.45"
            String orderId = message.split(",")[0].split(": ")[1].trim();
            BigDecimal amount = new BigDecimal(message.split(",")[1].split(": ")[1].trim());

            // Call the service to process the payment
            paymentService.processPayment(orderId, amount);

        } catch (Exception e) {
            System.err.println("Failed to parse order message: " + message);
            // Handle parsing error, maybe send to a dead-letter queue
        }
    }
}