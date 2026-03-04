package com.aditya.simple_web_app.web_app.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionDTO(
        UUID sessionId,
        String userAgent,
        String ipAddress,
        Instant createdDate
) {}
