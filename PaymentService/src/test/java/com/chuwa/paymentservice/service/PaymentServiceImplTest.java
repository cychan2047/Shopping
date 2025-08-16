package com.chuwa.paymentservice.service;

import com.chuwa.paymentservice.model.Payment;
import com.chuwa.paymentservice.model.PaymentStatus;
import com.chuwa.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPayment_whenPaymentDoesNotExist_shouldCreateNewPayment() {
        // Arrange
        String orderId = "order-123";
        BigDecimal amount = new BigDecimal("99.99");
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Payment payment = paymentService.processPayment(orderId, amount);

        // Assert
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void processPayment_whenPaymentExists_shouldReturnExistingPayment() {
        // Arrange (Idempotency Test)
        String orderId = "order-123";
        BigDecimal amount = new BigDecimal("99.99");
        Payment existingPayment = new Payment();
        existingPayment.setOrderId(orderId);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingPayment));

        // Act
        Payment payment = paymentService.processPayment(orderId, amount);

        // Assert
        assertThat(payment).isEqualTo(existingPayment);
        verify(paymentRepository, never()).save(any(Payment.class)); // Verify save is NOT called
    }

    @Test
    void submitPayment_whenPaymentIsPending_shouldCompletePayment() {
        // Arrange
        String orderId = "order-123";
        Payment pendingPayment = new Payment();
        pendingPayment.setOrderId(orderId);
        pendingPayment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Payment completedPayment = paymentService.submitPayment(orderId);

        // Assert
        assertThat(completedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository, times(1)).save(pendingPayment);
        verify(kafkaTemplate, times(1)).send(eq("payment-processed-topic"), anyString());
    }

    @Test
    void submitPayment_whenPaymentIsNotPending_shouldThrowException() {
        // Arrange
        String orderId = "order-123";
        Payment completedPayment = new Payment();
        completedPayment.setOrderId(orderId);
        completedPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(completedPayment));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            paymentService.submitPayment(orderId);
        });
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void reversePayment_whenPaymentIsCompleted_shouldSucceed() {
        // Arrange
        String orderId = "order-123";
        Payment completedPayment = new Payment();
        completedPayment.setOrderId(orderId);
        completedPayment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(completedPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Payment refundedPayment = paymentService.reversePayment(orderId);

        // Assert
        assertThat(refundedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository, times(1)).save(completedPayment);
        verify(kafkaTemplate, times(1)).send(eq("payment-refunded-topic"), anyString());
    }

    @Test
    void reversePayment_whenPaymentIsPending_shouldThrowException() {
        // Arrange
        String orderId = "order-123";
        Payment pendingPayment = new Payment();
        pendingPayment.setOrderId(orderId);
        pendingPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pendingPayment));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            paymentService.reversePayment(orderId);
        });

        // Verify that no save or send operation was performed
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }
}