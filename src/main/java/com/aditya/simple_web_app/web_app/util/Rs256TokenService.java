package com.aditya.simple_web_app.web_app.util;

import org.antlr.v4.runtime.Token;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


@Component
@Profile("oauth")
public class Rs256TokenService implements TokenService {
    @Override
    public String generateToken(UserDetails userDetails) {
        return "";
    }

    @Override
    public String extractUsername(String token) {
        return "";
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return false;
    }
}
