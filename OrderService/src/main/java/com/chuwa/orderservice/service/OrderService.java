package com.chuwa.orderservice.service;

import com.chuwa.orderservice.dto.OrderRequest;
import com.chuwa.orderservice.model.Order;
import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequest orderRequest);
    List<Order> findOrdersByUserId(Long userId);
}