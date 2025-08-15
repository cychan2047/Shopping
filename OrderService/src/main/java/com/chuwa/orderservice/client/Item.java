package com.chuwa.orderservice.client;

import lombok.Data;
import java.math.BigDecimal;

// This DTO represents the response from the ItemService
@Data
public class Item {
    private String id;
    private String itemName;
    private BigDecimal unitPrice;
    private int availableUnits;
}