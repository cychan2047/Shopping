package com.chuwa.itemservice.controller;

import com.chuwa.itemservice.dto.UpdateInventoryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chuwa.itemservice.model.Item;
import com.chuwa.itemservice.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class) // Loads only the controller layer
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc; // For simulating HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    @MockBean // Mocks the ItemService in the Spring context
    private ItemService itemService;

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        // Arrange
        Item item = new Item();
        item.setItemName("New Gadget");
        item.setUnitPrice(new BigDecimal("129.99"));

        when(itemService.createItem(any(Item.class))).thenReturn(item);

        // Act & Assert
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(jsonPath("$.itemName").value("New Gadget"));
    }

    @Test
    void getItemById_whenItemExists_shouldReturnItem() throws Exception {
        // Arrange
        Item item = new Item();
        item.setId("item-123");
        item.setItemName("Found Gadget");

        when(itemService.getItemById("item-123")).thenReturn(Optional.of(item));

        // Act & Assert
        mockMvc.perform(get("/api/items/item-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("Found Gadget"));
    }

    @Test
    void getItemById_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange
        when(itemService.getItemById("not-found-id")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/items/not-found-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateInventory_shouldReturnUpdatedItem() throws Exception {
        // Arrange
        UpdateInventoryRequest request = new UpdateInventoryRequest(-5);
        Item updatedItem = new Item();
        updatedItem.setId("item-123");
        updatedItem.setItemName("Updated Gadget");
        updatedItem.setAvailableUnits(15); // The new stock level after the update

        // Mock the service call
        when(itemService.updateInventory(eq("item-123"), eq(-5))).thenReturn(updatedItem);

        // Act & Assert
        mockMvc.perform(put("/api/items/item-123/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("item-123"))
                .andExpect(jsonPath("$.availableUnits").value(15));
    }
}