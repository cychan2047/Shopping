package com.chuwa.accountservice.controller;

import com.chuwa.accountservice.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chuwa.accountservice.entity.User;
import com.chuwa.accountservice.service.AccountService;
import com.chuwa.accountservice.util.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class) // Loads only the specified controller
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc; // A utility to simulate HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // For converting Java objects to JSON

    @MockBean // Creates a mock bean in the Spring context
    private AccountService accountService;

    // We must mock all dependencies of the controller
    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private AuthenticationManager authenticationManager;

    @Test
    void createAccount_shouldReturnCreatedUser() throws Exception {
        // Arrange
        User userToCreate = new User();
        userToCreate.setUserEmail("newuser@example.com");
        userToCreate.setPassword("password123");

        // When the service's createAccount method is called, return the same user
        when(accountService.createAccount(any(User.class))).thenReturn(userToCreate);

        // Act & Assert
        mockMvc.perform(post("/api/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userEmail").value("newuser@example.com"));
    }
}