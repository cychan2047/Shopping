package com.chuwa.accountservice.service;

import com.chuwa.accountservice.dto.UserUpdateRequest;
import com.chuwa.accountservice.entity.User;
import java.util.Optional;

public interface AccountService {
    User createAccount(User user);
    Optional<User> getAccountByEmail(String email);
    // Add the new update method
    User updateAccount(String email, UserUpdateRequest updateRequest);
}