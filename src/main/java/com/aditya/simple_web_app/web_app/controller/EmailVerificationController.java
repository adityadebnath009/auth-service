package com.aditya.simple_web_app.web_app.controller;


import com.aditya.simple_web_app.web_app.Domain.EmailVerificationToken;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.repository.EmailVerificationTokenRepository;
import com.aditya.simple_web_app.web_app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class EmailVerificationController {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public EmailVerificationController(EmailVerificationTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token)
    {
        Optional<EmailVerificationToken> verificationToken = Optional.of(tokenRepository
                .findByToken(token)
                .orElseThrow(
                        () -> new RuntimeException("Invalid token")
                ));

        EmailVerificationToken emailToken = verificationToken.get();

        if(emailToken.getExpiresAt().isBefore(Instant.now()))
        {
            throw new RuntimeException("Token expired");
        }

        User user = emailToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken.get());
        return ResponseEntity.ok("EMAIL_VERIFIED");
    }
}
