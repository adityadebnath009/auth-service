package com.aditya.simple_web_app.web_app.Domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
    @JoinColumn(
            name = "user_id", nullable = false,
            referencedColumnName = "id"
    )
    private User user;

    private Instant expiresAt;
}
