package com.chuwa.paymentservice.controller;

import com.chuwa.paymentservice.model.Payment;
import com.chuwa.paymentservice.model.PaymentStatus;
import com.chuwa.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void getPaymentByOrderId_whenPaymentExists_shouldReturnPayment() throws Exception {
        // Arrange
        String orderId = "order-123";
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(Optional.of(payment));

        // Act & Assert
        mockMvc.perform(get("/api/payments/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getPaymentByOrderId_whenPaymentDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange
        String orderId = "order-404";
        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/payments/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitPayment_shouldReturnProcessedPayment() throws Exception {
        // Arrange
        String orderId = "order-123";
        Payment completedPayment = new Payment();
        completedPayment.setOrderId(orderId);
        completedPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentService.submitPayment(orderId)).thenReturn(completedPayment);

        // Act & Assert
        mockMvc.perform(post("/api/payments/{orderId}/submit", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void refundPayment_shouldReturnRefundedPayment() throws Exception {
        // Arrange
        String orderId = "order-123";
        Payment refundedPayment = new Payment();
        refundedPayment.setOrderId(orderId);
        refundedPayment.setStatus(PaymentStatus.REFUNDED);
        when(paymentService.reversePayment(orderId)).thenReturn(refundedPayment);

        // Act & Assert
        mockMvc.perform(post("/api/payments/{orderId}/refund", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}