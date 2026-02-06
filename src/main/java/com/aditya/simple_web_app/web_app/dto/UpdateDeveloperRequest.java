package com.aditya.simple_web_app.web_app.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeveloperRequest(
        @NotBlank String name,
        @NotBlank String role
) {
}
