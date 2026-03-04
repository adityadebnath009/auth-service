package com.aditya.simple_web_app.web_app.service;


import com.aditya.simple_web_app.web_app.Domain.RefreshToken;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.repository.RefreshTokenRepository;
import org.apache.logging.log4j.CloseableThreadContext;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;


    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }


    public RefreshToken createRefreshToken(User user, String rawToken) {

        String hash = hash(rawToken);

        RefreshToken refreshToken = RefreshToken
                .builder()
                .tokenHash(hash)
                .revoked(false)
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdDate(Instant.now())
                .user(user)
                .build();
        return refreshTokenRepository.save(refreshToken);

    }

    public RefreshToken validateRefreshToken(String rawToken) {

        String hash = hash(rawToken);

        Optional<RefreshToken> tokenOpt =
                refreshTokenRepository.findByTokenHash(hash);

        RefreshToken token = tokenOpt.get();
        if(token.isRevoked()){
            revokeAllUserTokens(token.getUser());
            throw new RuntimeException("Old Refresh Token reuse is detected");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token is expired");
        }


        return token;
    }

    public void revokeToken(String rawToken) {

        String hash = hash(rawToken);

        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    public void revokeAllUserTokens(User user) {

        List<RefreshToken> tokens =
                refreshTokenRepository.findByUserAndRevokedFalse(user);

        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }

        refreshTokenRepository.saveAll(tokens);
    }


}
