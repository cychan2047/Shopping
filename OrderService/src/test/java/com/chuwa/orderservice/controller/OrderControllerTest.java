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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void getOrdersByUserId_shouldReturnOrders() throws Exception {
        // Arrange
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(1L);

        when(orderService.findOrdersByUserId(1L)).thenReturn(List.of(order));

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }
}