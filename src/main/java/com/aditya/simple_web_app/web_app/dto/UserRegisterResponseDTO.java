package com.aditya.simple_web_app.web_app.dto;

import com.aditya.simple_web_app.web_app.Domain.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserRegisterResponseDTO(
        UUID uuid,
        String email,
        Set<String> roles,
        Instant createdAt
) {
}
