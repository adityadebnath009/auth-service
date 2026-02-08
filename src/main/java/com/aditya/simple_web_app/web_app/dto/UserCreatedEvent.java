package com.aditya.simple_web_app.web_app.dto;

import java.util.UUID;

public record UserCreatedEvent(
        String email,
        UUID id
) {
}
