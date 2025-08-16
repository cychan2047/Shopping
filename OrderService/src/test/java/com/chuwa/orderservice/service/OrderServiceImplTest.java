package com.chuwa.orderservice.service;

import com.chuwa.orderservice.client.Item;
import com.chuwa.orderservice.client.ItemServiceClient;
import com.chuwa.orderservice.dto.OrderRequest;
import com.chuwa.orderservice.model.Order;
import com.chuwa.orderservice.model.OrderItem;
import com.chuwa.orderservice.model.OrderStatus;
import com.chuwa.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemServiceClient itemServiceClient;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_shouldSucceed() {
        // Arrange
        // 1. Create the incoming order request from a user
        OrderRequest.ProductInfo productInfo = new OrderRequest.ProductInfo();
        productInfo.setProductId("item-1");
        productInfo.setQuantity(2);
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(1L);
        orderRequest.setProducts(List.of(productInfo));

        // 2. Mock the response from the ItemService
        Item mockItem = new Item();
        mockItem.setId("item-1");
        mockItem.setUnitPrice(new BigDecimal("10.00"));
        mockItem.setAvailableUnits(100); // Plenty of stock
        when(itemServiceClient.getItemById("item-1")).thenReturn(mockItem);

        // 3. Mock the repository save operation
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order createdOrder = orderService.createOrder(orderRequest);

        // Assert
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(createdOrder.getItems().get(0).getProductId()).isEqualTo("item-1");

        // Verify that external services were called
        verify(itemServiceClient).getItemById("item-1");
        verify(itemServiceClient).updateInventory(eq("item-1"), any(ItemServiceClient.UpdateInventoryRequest.class));
        verify(orderRepository).save(any(Order.class));
        verify(kafkaTemplate).send(anyString(), anyString());
    }

    @Test
    void createOrder_whenStockIsInsufficient_shouldThrowException() {
        // Arrange
        OrderRequest.ProductInfo productInfo = new OrderRequest.ProductInfo();
        productInfo.setProductId("item-1");
        productInfo.setQuantity(10); // Requesting 10
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setProducts(List.of(productInfo));

        Item mockItem = new Item();
        mockItem.setAvailableUnits(5); // Only 5 in stock
        when(itemServiceClient.getItemById("item-1")).thenReturn(mockItem);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.createOrder(orderRequest);
        });

        // Verify that the order was never saved or published
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void cancelOrder_whenOrderIsCreated_shouldSucceed() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId("item-1");
        orderItem.setQuantity(2);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);
        existingOrder.setItems(List.of(orderItem));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Order cancelledOrder = orderService.cancelOrder(orderId);

        // Assert
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        // Verify that inventory was restocked (positive quantity)
        verify(itemServiceClient).updateInventory(eq("item-1"), argThat(req -> req.quantityChange() == 2));
        verify(orderRepository).save(existingOrder);
        verify(kafkaTemplate).send(eq("order-cancelled-topic"), anyString());
    }

    @Test
    void cancelOrder_whenOrderIsCompleted_shouldThrowException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.COMPLETED); // Set to a non-cancellable state

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.cancelOrder(orderId);
        });

        // Verify no external services were called
        verify(itemServiceClient, never()).updateInventory(anyString(), any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void findOrdersByUserId_shouldReturnSliceOfOrders() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 5); // Request first page, 5 items
        Order order = new Order();
        Slice<Order> expectedSlice = new SliceImpl<>(List.of(order), pageable, false);

        when(orderRepository.findByUserId(userId, pageable)).thenReturn(expectedSlice);

        // Act
        Slice<Order> result = orderService.findOrdersByUserId(userId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getNumber()).isEqualTo(0);
    }
}