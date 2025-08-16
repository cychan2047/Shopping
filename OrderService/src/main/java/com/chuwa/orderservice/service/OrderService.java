package com.chuwa.orderservice.service;

import com.chuwa.orderservice.dto.OrderRequest;
import com.chuwa.orderservice.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.UUID;


public interface OrderService {
    Order createOrder(OrderRequest orderRequest);
    Order cancelOrder(UUID orderId); // <-- Add this
    Slice<Order> findOrdersByUserId(Long userId, Pageable pageable);
}