package com.rentvideo.rentvideo.controller;

import com.rentvideo.rentvideo.dto.JwtResponse;
import com.rentvideo.rentvideo.dto.LoginRequest;
import com.rentvideo.rentvideo.dto.RegisterRequest;
import com.rentvideo.rentvideo.dto.UserResponse;
import com.rentvideo.rentvideo.service.AuthService;
import jakarta.validation.Valid; // Add this dependency if you haven't: starter-validation
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse registeredUser = authService.registerUser(registerRequest);
        // Use HttpStatus.CREATED for successful creation
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }
}