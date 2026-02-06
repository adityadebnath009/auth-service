package com.aditya.simple_web_app.web_app.dto;

import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
