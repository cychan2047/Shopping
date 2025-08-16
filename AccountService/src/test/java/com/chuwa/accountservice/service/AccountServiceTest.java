package com.chuwa.accountservice.service;

import com.chuwa.accountservice.config.SecurityConfig;
import com.chuwa.accountservice.dto.UserUpdateRequest;
import com.chuwa.accountservice.entity.User;
import com.chuwa.accountservice.repository.UserRepository;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito for JUnit 5
@Import(SecurityConfig.class)
class AccountServiceTest {

    @Mock // Creates a mock instance of UserRepository
    private UserRepository userRepository;

    @Mock // Creates a mock instance of PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @InjectMocks // Creates an instance of AccountService and injects the mocks into it
    private AccountServiceImpl accountService;

    @Test
    void createAccount_shouldSaveUserWithEncodedPassword() {
        // Arrange: Set up the test data and mock behavior
        User user = new User();
        user.setUserEmail("test@example.com");
        user.setPassword("password123");

        String hashedPassword = "hashedPassword123";

        // Define what the mocks should do when called
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act: Call the method we want to test
        User createdUser = accountService.createAccount(user);

        // Assert: Verify the results
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getPassword()).isEqualTo(hashedPassword);

        // Verify that the save method on the repository was called exactly once
        verify(userRepository, times(1)).save(user);
        // Verify that the encode method was called exactly once
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void updateAccount_whenUserExists_shouldUpdateFields() {
        // Arrange
        String email = "test@example.com";
        User existingUser = new User();
        existingUser.setUserEmail(email);
        existingUser.setUserName("Old Name");

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUserName("New Name");
        updateRequest.setShippingAddress("123 New Address");

        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        User updatedUser = accountService.updateAccount(email, updateRequest);

        // Assert
        assertThat(updatedUser.getUserName()).isEqualTo("New Name");
        assertThat(updatedUser.getShippingAddress()).isEqualTo("123 New Address");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateAccount_whenUserNotFound_shouldThrowException() {
        // Arrange
        String email = "notfound@example.com";
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            accountService.updateAccount(email, updateRequest);
        });
    }
}