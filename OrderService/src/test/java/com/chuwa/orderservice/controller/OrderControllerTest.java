package com.chuwa.orderservice.controller;

import com.chuwa.orderservice.dto.OrderRequest;
import com.chuwa.orderservice.model.Order;
import com.chuwa.orderservice.model.OrderStatus;
import com.chuwa.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrder_shouldReturnCreatedOrder() throws Exception {
        // Arrange
        OrderRequest orderRequest = new OrderRequest(); // Assume this is populated
        Order createdOrder = new Order();
        createdOrder.setId(UUID.randomUUID());
        createdOrder.setUserId(1L);
        createdOrder.setStatus(OrderStatus.CREATED);

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(createdOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void cancelOrder_shouldReturnCancelledOrder() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order cancelledOrder = new Order();
        cancelledOrder.setId(orderId);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(orderId)).thenReturn(cancelledOrder);

        // Act & Assert
        mockMvc.perform(put("/api/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getOrdersByUserId_shouldReturnPaginatedOrders() throws Exception {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 1);
        Order order = new Order();
        order.setUserId(userId);
        Slice<Order> orderSlice = new SliceImpl<>(List.of(order), pageable, false);

        when(orderService.findOrdersByUserId(eq(userId), any(Pageable.class))).thenReturn(orderSlice);

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                // Check that the response contains the 'content' array for the items
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(userId))
                // Check the pagination metadata
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.size").value(1));
    }
}