package com.aditya.simple_web_app.web_app.auth.Domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID sessionId;
    @PrePersist
    public void generateSessionId() {
        if (this.sessionId == null) {
            this.sessionId = UUID.randomUUID();
        }
    }

    @Column(nullable = false, unique = true)
    private String tokenHash;



    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;




    private String deviceName;
    private String userAgent;
    private String ipAddress;

}
