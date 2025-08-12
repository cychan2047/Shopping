package com.chuwa.itemservice.dto;

// A simple record to hold the quantity to add or remove from inventory.
// A negative number will decrease stock, a positive number will increase it.
public record UpdateInventoryRequest(int quantityChange) {
}