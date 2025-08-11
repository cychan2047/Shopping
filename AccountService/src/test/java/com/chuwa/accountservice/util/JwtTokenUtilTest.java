package com.chuwa.accountservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUp() {
        // We create the utility manually with test values
        String testSecret = "a-very-long-and-secure-secret-key-for-testing-purposes-only";
        jwtTokenUtil = new JwtTokenUtil(testSecret);
        // Manually set expiration for consistency in tests
        // ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpirationInMs", 3600000L); // Optional
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        // Arrange
        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());

        // Act
        String token = jwtTokenUtil.generateToken(userDetails);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        String usernameFromToken = jwtTokenUtil.getUsernameFromToken(token);
        assertThat(usernameFromToken).isEqualTo("test@example.com");
        assertThat(jwtTokenUtil.validateToken(token, userDetails)).isTrue();
    }
}