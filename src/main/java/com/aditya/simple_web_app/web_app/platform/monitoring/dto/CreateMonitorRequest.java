package com.aditya.simple_web_app.web_app.platform.monitoring.dto;


import jakarta.validation.constraints.*;

public record CreateMonitorRequest(
        @NotBlank
        String name,
        @NotBlank
        String url,
        @NotBlank
        String method,

        @Min(100)
        @Max(599)
        Integer expectedStatus,
        @Positive
        Integer timeoutMs,
        @Positive
        Integer intervalSeconds

){}
