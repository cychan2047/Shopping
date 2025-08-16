package com.chuwa.accountservice.service;

import com.chuwa.accountservice.dto.UserUpdateRequest;
import com.chuwa.accountservice.entity.User;
import com.chuwa.accountservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createAccount(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getAccountByEmail(String email) {
        return userRepository.findByUserEmail(email);
    }

    @Override
    public User updateAccount(String email, UserUpdateRequest updateRequest) {
        // Find the existing user or throw an exception
        User existingUser = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Update fields only if new values are provided
        if (updateRequest.getUserName() != null) {
            existingUser.setUserName(updateRequest.getUserName());
        }
        if (updateRequest.getShippingAddress() != null) {
            existingUser.setShippingAddress(updateRequest.getShippingAddress());
        }
        if (updateRequest.getBillingAddress() != null) {
            existingUser.setBillingAddress(updateRequest.getBillingAddress());
        }
        // If a new password is provided, encode it before saving
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        // Save and return the updated user
        return userRepository.save(existingUser);
    }
}