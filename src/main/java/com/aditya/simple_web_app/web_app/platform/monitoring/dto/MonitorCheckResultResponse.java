package com.aditya.simple_web_app.web_app.platform.monitoring.dto;

import java.time.Instant;

public record MonitorCheckResultResponse(
        Long id,
        Instant checkedAt,
        Integer statusCode,
        Integer latencyMs,
        boolean success,
        String errorMessage,
        Long monitorId

) {
}
