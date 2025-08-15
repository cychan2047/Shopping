package com.chuwa.orderservice.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import java.math.BigDecimal;

@Data
@UserDefinedType("order_item") // Defines this class as a Cassandra User-Defined Type
public class OrderItem {

    private String productId;
    private int quantity;
    private BigDecimal priceAtPurchase; // The price when the order was made
}