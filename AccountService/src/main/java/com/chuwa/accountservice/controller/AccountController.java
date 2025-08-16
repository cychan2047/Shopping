package com.chuwa.accountservice.controller;

import com.chuwa.accountservice.dto.AuthRequest;
import com.chuwa.accountservice.dto.AuthResponse;
import com.chuwa.accountservice.dto.UserUpdateRequest;
import com.chuwa.accountservice.entity.User;
import com.chuwa.accountservice.service.AccountService;
import com.chuwa.accountservice.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Service", description = "APIs for user account management and authentication")
public class AccountController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "Register a new user account")
    @ApiResponse(responseCode = "200", description = "Account created successfully")
    @PostMapping("/register")
    public ResponseEntity<User> createAccount(@RequestBody User user) {
        User createdUser = accountService.createAccount(user);
        return ResponseEntity.ok(createdUser);
    }

    @Operation(summary = "Authenticate a user and receive a JWT token")
    @ApiResponse(responseCode = "200", description = "Authentication successful, token returned")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );
        final var userDetails = authentication.getPrincipal();
        final String token = jwtTokenUtil.generateToken((org.springframework.security.core.userdetails.UserDetails) userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Operation(summary = "Look up an account by its email address")
    @ApiResponse(responseCode = "200", description = "Account found")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @GetMapping("/{email}")
    public ResponseEntity<User> getAccount(@PathVariable String email) {
        return accountService.getAccountByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update an existing user account")
    @ApiResponse(responseCode = "200", description = "Account updated successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "403", description = "Forbidden to update another user's account")
    @PutMapping("/{email}")
    public ResponseEntity<User> updateAccount(@PathVariable String email,
                                              @RequestBody UserUpdateRequest updateRequest,
                                              Authentication authentication) {

        // Security Check: Ensure the authenticated user is the one they are trying to update
        if (!authentication.getName().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            User updatedUser = accountService.updateAccount(email, updateRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}