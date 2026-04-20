package com.aditya.simple_web_app.web_app.config;


import com.aditya.simple_web_app.web_app.auth.dto.StripeProperties;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    public StripeConfig(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }

    private final StripeProperties stripeProperties;


    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.secretKey();
    }
}
