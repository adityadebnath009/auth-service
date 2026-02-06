package com.aditya.simple_web_app.web_app.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;

public record UserRegisterDTO(

        @Email
        @Column(unique = true, nullable = false)
        String email,

        @Column(unique = true, nullable = false)
        String password
) {
}
