package com.aditya.simple_web_app.web_app.util;

import org.antlr.v4.runtime.Token;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


@Component
@Profile("oauth")
public class Rs256TokenService implements TokenService {

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        return "";
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        return "";
    }

    @Override
    public String extractUsernameFromRefreshToken(String token) {
        return "";
    }

    @Override
    public String extractUserNameFromAccessToken(String token) {
        return "";
    }

    @Override
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return false;
    }

    @Override
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return false;
    }
}
