package com.chuwa.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name should match the target service's application name
// url points to where the ItemService is running
@FeignClient(name = "item-service", url = "http://localhost:8082/api/items")
public interface ItemServiceClient {

    @GetMapping("/{id}")
    Item getItemById(@PathVariable String id);

    // We'll need a simple request DTO for the body of this PUT request
    record UpdateInventoryRequest(int quantityChange) {}

    @PutMapping("/{id}/inventory")
    void updateInventory(@PathVariable String id, @RequestBody UpdateInventoryRequest request);
}