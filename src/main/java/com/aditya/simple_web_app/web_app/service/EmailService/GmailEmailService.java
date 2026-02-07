package com.aditya.simple_web_app.web_app.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class GmailEmailService implements EmailService{

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public GmailEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendWelcomeEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Welcome to the App");
        message.setText(
                """
                Welcome!

                Your account has been created successfully.
                You can now log in and start using the app.

                — Team
                """
        );

        mailSender.send(message);
    }

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verify your email");
        message.setText(
                """
                Please verify your email by clicking the link below:

                %s

                This link will expire soon.
                """
                        .formatted(verificationLink)
        );

        mailSender.send(message);
    }
}