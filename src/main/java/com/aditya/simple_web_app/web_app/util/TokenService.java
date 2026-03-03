package com.aditya.simple_web_app.web_app.util;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;


public interface TokenService {

    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);


    String extractUsernameFromRefreshToken(String token);
    String extractUserNameFromAccessToken(String token);
    boolean isAccessTokenValid(String token, UserDetails userDetails);
    boolean isRefreshTokenValid(String token, UserDetails userDetails);
}
