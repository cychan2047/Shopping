package com.chuwa.accountservice.controller;

import lombok.*;
import com.chuwa.accountservice.dto.AuthRequest;
import com.chuwa.accountservice.dto.AuthResponse;
import com.chuwa.accountservice.entity.User;
import com.chuwa.accountservice.service.AccountService;
import com.chuwa.accountservice.util.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;


    // ... Autowire AuthenticationManager and JWT utility ...

    @PostMapping("/register")
    public ResponseEntity<?> createAccount(@RequestBody User user) {
        User createdUser = accountService.createAccount(user);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        // Spring Security's AuthenticationManager handles the password verification
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );

        // If authentication succeeds, we get the principal (the user details)
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generate a JWT token for the authenticated user
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Return the token in the response body
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // This is the lookup API
    @GetMapping("/{email}")
    public ResponseEntity<User> getAccount(@PathVariable String email) {
        return accountService.getAccountByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add an endpoint for login that returns a JWT token
}