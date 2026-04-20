package com.aditya.simple_web_app.web_app.auth.dto;

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
