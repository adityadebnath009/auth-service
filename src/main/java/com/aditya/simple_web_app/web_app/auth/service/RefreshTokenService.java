package com.aditya.simple_web_app.web_app.auth.service;


import com.aditya.simple_web_app.web_app.auth.Domain.RefreshToken;
import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ua_parser.Client;
import ua_parser.Parser;

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


    public RefreshToken createRefreshToken(User user, String rawToken, String userAgent, String ipAddress) {

        String hash = hash(rawToken);
        String deviceName = resolveDeviceName(userAgent);

        RefreshToken refreshToken = RefreshToken
                .builder()
                .tokenHash(hash)
                .revoked(false)
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdDate(Instant.now())
                .user(user)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
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

    public List<RefreshToken> getAllTokens(User user) {

        return refreshTokenRepository.findByUserAndRevokedFalse(user);

    }

    public void revokeBySessionId(UUID sessionId, User user) {

        RefreshToken token = refreshTokenRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("SessionID not found"));


        //Its an important part to take care of that the authenticated user is revoking the session
        if(!token.getUser().getId().equals(user.getId())){
            throw new RuntimeException("UNAUTHORIZED SESSION REVOKING ATTEMPT");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

    }

    private String resolveDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown Device";
        }

        try {
            Client client = new Parser().parse(userAgent);
            String browser = client.userAgent != null ? client.userAgent.family : "Unknown Browser";
            String os = client.os != null ? client.os.family : "Unknown OS";
            return browser + " on " + os;
        } catch (Exception ex) {
            return "Unknown Device";
        }
    }


}
