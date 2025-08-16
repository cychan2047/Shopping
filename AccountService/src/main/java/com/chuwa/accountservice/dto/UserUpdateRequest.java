package com.chuwa.accountservice.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    // Users can update their name, password, and addresses.
    // The email is the identifier and cannot be changed.
    private String userName;
    private String password; // Should be null if not changing
    private String shippingAddress;
    private String billingAddress;
}