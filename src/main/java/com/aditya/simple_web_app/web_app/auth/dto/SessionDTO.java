package com.aditya.simple_web_app.web_app.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionDTO(
        UUID sessionId,
        String deviceName,
        String ipAddress,

        Instant createdDate

) {}
