package com.chuwa.accountservice.service;

import com.chuwa.accountservice.entity.User;
import com.chuwa.accountservice.repository.UserRepository;
import org.testng.annotations.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito for JUnit 5
class AccountServiceTest {

    @Mock // Creates a mock instance of UserRepository
    private UserRepository userRepository;

    @Mock // Creates a mock instance of PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @InjectMocks // Creates an instance of AccountService and injects the mocks into it
    private AccountService accountService;

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
}