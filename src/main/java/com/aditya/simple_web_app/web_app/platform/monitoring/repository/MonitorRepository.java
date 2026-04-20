package com.aditya.simple_web_app.web_app.platform.monitoring.repository;

import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.platform.monitoring.domain.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {

    List<Monitor> findAllByOwner(User owner);

    Optional<Monitor> findByIdAndOwner(Long id, User owner);

    List<Monitor> findAllByActiveTrue();

    boolean existsByIdAndOwner(Long id, User owner);
}
