package com.aditya.simple_web_app.web_app.util;

import com.aditya.simple_web_app.web_app.repository.RefreshTokenRepository;
import com.aditya.simple_web_app.web_app.service.RefreshTokenService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
@EnableScheduling
public class RefreshTokenScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenScheduler(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
    public void cleanup() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
    }
}
