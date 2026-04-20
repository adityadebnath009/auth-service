package com.aditya.simple_web_app.web_app.auth.service;

import com.aditya.simple_web_app.web_app.auth.Domain.EmailVerificationToken;
import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.auth.repository.EmailVerificationTokenRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;


@Component
public class EmailVerificationTokenService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public EmailVerificationTokenService(EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;


    }

    public String generateToken(User user) {

        emailVerificationTokenRepository.findByUser(user).ifPresent(emailVerificationToken -> {
            emailVerificationTokenRepository.delete(emailVerificationToken);
        });

        String rawToken = UUID.randomUUID().toString();

        String hashToken = hashToken(rawToken);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenHash(hashToken)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(24 * 60 * 60))
                .used(false)
                .build();



        emailVerificationTokenRepository.save(token);

        return rawToken;


    }
    public Optional<EmailVerificationToken> validateToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        return emailVerificationTokenRepository.findByTokenHash(tokenHash)
                .filter(token -> !token.isUsed())
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()));
    }

    public void markUsed(EmailVerificationToken token) {
        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
    }
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
