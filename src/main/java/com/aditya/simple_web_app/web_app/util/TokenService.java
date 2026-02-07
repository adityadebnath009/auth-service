package com.aditya.simple_web_app.web_app.util;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;



public interface TokenService {

    String generateToken(UserDetails userDetails);

    String extractUsername(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
}
