package com.aditya.simple_web_app.web_app.platform.monitoring.dto;

public record PatchMonitorRequest(
        String name,
        String url,
        String method,
        Integer expectedStatus,
        Integer timeoutMs,
        Integer intervalSeconds,
        Boolean active
) {
}
