package com.chuwa.itemservice.controller;

import com.chuwa.itemservice.dto.UpdateInventoryRequest;
import com.chuwa.itemservice.model.Item;
import com.chuwa.itemservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Handles the HTTP POST request to create a new item.
     * @param item The item data sent in the request body.
     * @return The created item with an HTTP 201 Created status.
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        Item createdItem = itemService.createItem(item);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    /**
     * Handles the HTTP GET request to retrieve an item by its ID.
     * @param id The ID of the item, passed as a path variable.
     * @return The found item with an HTTP 200 OK status, or a 404 Not Found status.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable String id) {
        return itemService.getItemById(id)
                .map(ResponseEntity::ok) // If item is present, return 200 OK
                .orElse(ResponseEntity.notFound().build()); // Otherwise, return 404 Not Found
    }

    @PutMapping("/{id}/inventory")
    public ResponseEntity<Item> updateInventory(@PathVariable String id, @RequestBody UpdateInventoryRequest request) {
        Item updatedItem = itemService.updateInventory(id, request.quantityChange());
        return ResponseEntity.ok(updatedItem);
    }
}