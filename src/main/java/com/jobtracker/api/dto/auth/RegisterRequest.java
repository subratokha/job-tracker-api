package com.jobtracker.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Invalid Email format")
        String email,
        @NotBlank(message = "password is mandatory")
        @Size(min = 8, max = 12, message = "Password must be at least 8 characters and maximum 12 characters long")
        String password,
        @NotBlank(message = "First Name is mandatory")
        String firstName,
        String lastName
) {
}