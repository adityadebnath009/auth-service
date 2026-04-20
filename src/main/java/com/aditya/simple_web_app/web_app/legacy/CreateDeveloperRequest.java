package com.aditya.simple_web_app.web_app.legacy;

import jakarta.validation.constraints.NotBlank;

public record CreateDeveloperRequest(
        @NotBlank(message = "Name should not be null")
        String name,
        @NotBlank(message = "Role should be a valid role")
        String role
) {}
