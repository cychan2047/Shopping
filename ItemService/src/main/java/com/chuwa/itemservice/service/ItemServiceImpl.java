package com.chuwa.itemservice.service;

import com.chuwa.itemservice.model.Item;
import com.chuwa.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public Item createItem(Item item) {
        // Here you could add validation logic before saving
        return itemRepository.save(item);
    }

    @Override
    public Optional<Item> getItemById(String id) {
        return itemRepository.findById(id);
    }

    @Override
    public Item updateInventory(String id, int quantityChange) {
        // 1. Find the item in the database
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));

        // 2. Calculate the new stock level
        int newStock = item.getAvailableUnits() + quantityChange;

        // 3. Validate that stock does not go below zero
        if (newStock < 0) {
            throw new IllegalStateException("Insufficient stock for item: " + item.getItemName());
        }

        // 4. Update the stock and save the item
        item.setAvailableUnits(newStock);
        return itemRepository.save(item);
    }
}