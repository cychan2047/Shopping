package com.chuwa.itemservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.List;

@Data
@Document(collection = "items")
public class Item {

    @Id
    private String id; // MongoDB uses String IDs by default

    private String itemName;
    private BigDecimal unitPrice;
    private List<String> pictureUrls;
    private String upc; // Universal Product Code
    private int availableUnits; // For inventory management
}