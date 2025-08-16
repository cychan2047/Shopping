package com.chuwa.orderservice.controller;

import com.chuwa.orderservice.dto.OrderRequest;
import com.chuwa.orderservice.model.Order;
import com.chuwa.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Handles the HTTP POST request to create a new order.
     * The request body should contain the userId and a list of products.
     * @param orderRequest The order data sent in the request body.
     * @return The created order with an HTTP 201 Created status.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest) {
        Order createdOrder = orderService.createOrder(orderRequest);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    /**
     * Handles the HTTP GET request to retrieve all orders for a specific user.
     * @param userId The ID of the user, passed as a path variable.
     * @return A list of orders belonging to the user with an HTTP 200 OK status.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Slice<Order>> getOrdersByUserId(@PathVariable Long userId, Pageable pageable) {
        Slice<Order> orders = orderService.findOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId) {
        try {
            Order cancelledOrder = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(cancelledOrder);
        } catch (Exception e) {
            // Handle exceptions like order not found or invalid state
            return ResponseEntity.badRequest().build();
        }
    }
}