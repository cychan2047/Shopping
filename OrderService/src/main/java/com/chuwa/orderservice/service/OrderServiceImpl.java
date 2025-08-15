package com.chuwa.orderservice.service;

import com.chuwa.orderservice.client.ItemServiceClient;
import com.chuwa.orderservice.dto.OrderRequest;
import com.chuwa.orderservice.model.Order;
import com.chuwa.orderservice.model.OrderItem;
import com.chuwa.orderservice.model.OrderStatus;
import com.chuwa.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemServiceClient itemServiceClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. Verify each product's availability and price from ItemService
        for (OrderRequest.ProductInfo p : orderRequest.getProducts()) {
            // Call ItemService using Feign client
            var item = itemServiceClient.getItemById(p.getProductId());

            if (item == null) {
                throw new IllegalArgumentException("Product not found: " + p.getProductId());
            }
            if (item.getAvailableUnits() < p.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + item.getItemName());
            }

            // Create an OrderItem with the price at time of purchase
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(p.getProductId());
            orderItem.setQuantity(p.getQuantity());
            orderItem.setPriceAtPurchase(item.getUnitPrice());
            orderItems.add(orderItem);

            totalAmount = totalAmount.add(item.getUnitPrice().multiply(BigDecimal.valueOf(p.getQuantity())));
        }

        // 2. Create and save the order
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(orderRequest.getUserId());
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());
        orderRepository.save(order);

        // 3. Update inventory in ItemService for each product
        for (OrderItem oi : orderItems) {
            var request = new ItemServiceClient.UpdateInventoryRequest(-oi.getQuantity());
            itemServiceClient.updateInventory(oi.getProductId(), request);
        }

        // 4. Publish an event to Kafka
        kafkaTemplate.send("order-created-topic", "Order created with ID: " + order.getId());

        return order;
    }

    @Override
    public List<Order> findOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}