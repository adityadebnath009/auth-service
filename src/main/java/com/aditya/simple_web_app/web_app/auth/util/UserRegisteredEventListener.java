package com.aditya.simple_web_app.web_app.auth.util;

import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.auth.dto.UserCreatedEvent;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component

public class UserRegisteredEventListener {

    private final JavaMailSender mailSender;

    public UserRegisteredEventListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @Async
    @EventListener
    public void handleUserRegistered(UserCreatedEvent event) {
        User user = event.user();
        String rawToken = event.token();

        String verificationLink = "http://localhost:8080/auth/verify/" + rawToken;
        System.out.println("Verification link: " + verificationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.getEmail());
            helper.setSubject("Verify your BackendForge email");
            helper.setText(buildEmailBody(user, verificationLink), true); // true = HTML

            mailSender.send(message);
        } catch (Exception e) {
            // Log but don't crash — email failure shouldn't break registration
            System.err.println("Failed to send verification email to " + user.getEmail() + ": " + e.getMessage());
        }
    }

    private String buildEmailBody(User user, String verificationLink) {
        return """
                <html>
                <body style="font-family: sans-serif;">
                    <h2>Welcome to BackendForge</h2>
                    <p>Thanks for registering. Please verify your email by clicking the link below:</p>
                    <a href="%s" style="
                        display: inline-block;
                        padding: 12px 24px;
                        background-color: #e8ff47;
                        color: #0a0a0f;
                        text-decoration: none;
                        font-weight: bold;
                        border-radius: 4px;
                    ">Verify Email</a>
                    <p>This link expires in 24 hours.</p>
                    <p>If you did not create an account, ignore this email.</p>
                </body>
                </html>
                """.formatted(verificationLink);
    }

}
