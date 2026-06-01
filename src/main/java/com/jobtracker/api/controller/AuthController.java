package com.jobtracker.api.controller;

import com.jobtracker.api.dto.auth.AuthRequest;
import com.jobtracker.api.dto.auth.AuthResponse;
import com.jobtracker.api.dto.auth.RegisterRequest;
import com.jobtracker.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name= "Authentication", description = "User registration and login")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode="201", description = "User registered successfully")
    @ApiResponse(responseCode = "400",description = "Validation Failure")
    @ApiResponse(responseCode = "409", description = "Email already exists")
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterRequest registerRequest) {
        userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Login and get JWT token")
    @ApiResponse(responseCode="200", description = "User logged in successfully")
    @ApiResponse(responseCode = "401",description = "Invalid Credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody @Valid AuthRequest authRequest) {
        AuthResponse authResponse = userService.loginUser(authRequest);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }
}
