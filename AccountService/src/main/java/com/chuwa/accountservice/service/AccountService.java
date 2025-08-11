package com.chuwa.accountservice.service;

import com.chuwa.accountservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.chuwa.accountservice.entity.User;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public User createAccount(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt password
        return userRepository.save(user);
    }

    public Optional<User> getAccountByEmail(String email) {
        return userRepository.findByUserEmail(email);
    }

    public User updateAccount(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public void deleteAccount(Long id) {
        userRepository.deleteById(id);
    }
}