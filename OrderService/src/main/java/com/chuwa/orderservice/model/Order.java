package com.chuwa.orderservice.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Frozen; // Important import

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Table("orders") // Maps this class to the 'orders' table in Cassandra
public class Order {

    @PrimaryKey // Marks this field as the primary key
    private UUID id;

    @Column("user_id")
    private Long userId;

    // The @Frozen annotation is crucial. It tells Cassandra to treat the list
    // as a single, immutable collection.
    @Frozen
    private List<OrderItem> items;

    private BigDecimal totalAmount;

    private OrderStatus status;

    @Column("created_at")
    private Instant createdAt;
}