package com.aditya.simple_web_app.web_app.auth.dto;

public record OAuthUserInfo(
        String providerId,
        String email,
        String name,
        String profilePicture
) {
}
