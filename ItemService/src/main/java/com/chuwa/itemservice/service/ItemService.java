package com.chuwa.itemservice.service;

import com.chuwa.itemservice.model.Item;
import java.util.Optional;

public interface ItemService {
    Item createItem(Item item);
    Optional<Item> getItemById(String id);
    Item updateInventory(String id, int quantityChange);
}
