package com.chuwa.itemservice.service;

import com.chuwa.itemservice.model.Item;
import com.chuwa.itemservice.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito for JUnit 5
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_shouldSaveAndReturnItem() {
        // Arrange: Create a test item
        Item newItem = new Item();
        newItem.setItemName("Test Item");
        newItem.setUnitPrice(new BigDecimal("99.99"));

        // Define the behavior of the mock repository
        when(itemRepository.save(any(Item.class))).thenReturn(newItem);

        // Act: Call the service method
        Item savedItem = itemService.createItem(newItem);

        // Assert: Check the results
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getItemName()).isEqualTo("Test Item");
        verify(itemRepository).save(newItem); // Verify the save method was called
    }

    @Test
    void getItemById_whenItemExists_shouldReturnItem() {
        // Arrange
        Item item = new Item();
        item.setId("test-id");
        item.setItemName("Existing Item");

        when(itemRepository.findById("test-id")).thenReturn(Optional.of(item));

        // Act
        Optional<Item> foundItem = itemService.getItemById("test-id");

        // Assert
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getItemName()).isEqualTo("Existing Item");
    }

    @Test
    void getItemById_whenItemDoesNotExist_shouldReturnEmpty() {
        // Arrange
        when(itemRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // Act
        Optional<Item> foundItem = itemService.getItemById("non-existent-id");

        // Assert
        assertThat(foundItem).isNotPresent();
    }

    @Test
    void updateInventory_shouldUpdateStockAndReturnItem() {
        // Arrange
        Item existingItem = new Item();
        existingItem.setId("item-1");
        existingItem.setAvailableUnits(10); // Start with 10 units

        when(itemRepository.findById("item-1")).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        // Act: Decrease stock by 4
        Item updatedItem = itemService.updateInventory("item-1", -4);

        // Assert
        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getAvailableUnits()).isEqualTo(6); // 10 - 4 = 6
        verify(itemRepository).save(existingItem);
    }

    @Test
    void updateInventory_whenItemNotFound_shouldThrowException() {
        // Arrange
        when(itemRepository.findById("not-found-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.updateInventory("not-found-id", -5);
        });
        verify(itemRepository, never()).save(any(Item.class)); // Ensure save was never called
    }

    @Test
    void updateInventory_whenStockIsInsufficient_shouldThrowException() {
        // Arrange
        Item existingItem = new Item();
        existingItem.setId("item-1");
        existingItem.setAvailableUnits(5); // Start with 5 units

        when(itemRepository.findById("item-1")).thenReturn(Optional.of(existingItem));

        // Act & Assert: Try to decrease stock by 10
        assertThrows(IllegalStateException.class, () -> {
            itemService.updateInventory("item-1", -10);
        });
        verify(itemRepository, never()).save(any(Item.class)); // Ensure save was never called
    }
}