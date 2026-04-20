package com.aditya.simple_web_app.web_app.platform.monitoring.domain;


import com.aditya.simple_web_app.web_app.auth.Domain.User;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "monitors")
public class Monitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)

    private String currentStatus;
    @Column(nullable = false)
    private String method;
    @Column(nullable = false)
    private Integer expectedStatus;
    @Column(nullable = false)
    private Integer timeoutMs;
    @Column(nullable = false)
    private Integer intervalSeconds;
    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

}
