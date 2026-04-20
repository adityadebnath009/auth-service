package com.aditya.simple_web_app.web_app.auth.dto;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe")
public record StripeProperties(
        String secretKey,
        String webhook,
        String proPriceId

) {
}
