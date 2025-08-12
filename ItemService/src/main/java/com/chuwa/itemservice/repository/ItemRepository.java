package com.chuwa.itemservice.repository;

import com.chuwa.itemservice.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String> {

}