package com.aditya.simple_web_app.web_app.service.EmailService;


import com.aditya.simple_web_app.web_app.dto.UserCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class UserEmailListener {

    private final EmailService emailService;

    public UserEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void onUserCreated(UserCreatedEvent userCreatedEvent)

    {
        emailService.sendWelcomeEmail(userCreatedEvent.email());

    }
}
