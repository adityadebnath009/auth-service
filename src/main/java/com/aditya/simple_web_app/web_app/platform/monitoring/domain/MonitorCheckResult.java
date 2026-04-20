package com.aditya.simple_web_app.web_app.platform.monitoring.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "monitor_check_results")
public class MonitorCheckResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant checkedAt;

    @Column
    private Integer statusCode;

    @Column(nullable = false)
    private Integer latencyMs;
    @Column(nullable = false)
    private boolean success;

    @Column
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;
}
