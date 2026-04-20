package com.aditya.simple_web_app.web_app.platform.monitoring.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMonitorRequest(
        @NotBlank
        String name,
        @NotBlank
        String url,
        @NotBlank
        String method,
        @Size(min = 100, max = 599)
        Integer expectedStatus,
        @Positive
        Integer timeoutMs,
        @Positive
        Integer intervalSeconds

){}
