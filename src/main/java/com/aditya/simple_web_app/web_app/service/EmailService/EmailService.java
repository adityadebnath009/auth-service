package com.aditya.simple_web_app.web_app.service.EmailService;

public interface EmailService {

    void sendWelcomeEmail(String to);
    void sendVerificationEmail(String to, String token);
}
