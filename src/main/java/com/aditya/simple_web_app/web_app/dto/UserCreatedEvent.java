package com.aditya.simple_web_app.web_app.dto;

import com.aditya.simple_web_app.web_app.Domain.User;

import java.util.UUID;

public record UserCreatedEvent(

        User user,
        String token
) {
}
