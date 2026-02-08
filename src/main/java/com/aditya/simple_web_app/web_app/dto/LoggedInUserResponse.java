package com.aditya.simple_web_app.web_app.dto;



import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record LoggedInUserResponse(

        UUID id,
        String email,
        Set<String> roles,
        Instant createdAt

        ) {
}
