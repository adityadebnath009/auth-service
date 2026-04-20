package com.aditya.simple_web_app.web_app.auth.dto;

import com.aditya.simple_web_app.web_app.auth.Domain.User;

public record UserCreatedEvent(

        User user,
        String token
) {
}
