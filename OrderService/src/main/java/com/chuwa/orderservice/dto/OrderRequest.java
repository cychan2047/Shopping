package com.chuwa.orderservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private List<ProductInfo> products;

    @Data
    public static class ProductInfo {
        private String productId;
        private int quantity;
    }
}