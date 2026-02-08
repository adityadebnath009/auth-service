package com.aditya.simple_web_app.web_app.service.EmailService;


import com.aditya.simple_web_app.web_app.Domain.EmailVerificationToken;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.dto.UserCreatedEvent;
import com.aditya.simple_web_app.web_app.repository.EmailVerificationTokenRepository;
import com.aditya.simple_web_app.web_app.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Component
public class VerificationEmailListener {


    private final EmailVerificationTokenRepository repository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public VerificationEmailListener(EmailVerificationTokenRepository repository, EmailService emailService, UserRepository userRepository) {
        this.repository = repository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }


    @Async
    @EventListener
    public void onUserCreated(UserCreatedEvent event)
    {
        String token = UUID.randomUUID().toString();
        Optional<User> user = userRepository.findById(event.id());
        EmailVerificationToken verificationToken = EmailVerificationToken
                .builder()
                .token(token)
                .user(user.get())
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        repository.save(verificationToken);

        String link = "http://localhost:8080/auth/verify?token=" + token;

        emailService.sendVerificationEmail(event.email(), link);

    }
}
