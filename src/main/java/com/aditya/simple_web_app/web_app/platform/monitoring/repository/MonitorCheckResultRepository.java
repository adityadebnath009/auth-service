package com.aditya.simple_web_app.web_app.platform.monitoring.repository;

import com.aditya.simple_web_app.web_app.platform.monitoring.domain.Monitor;
import com.aditya.simple_web_app.web_app.platform.monitoring.domain.MonitorCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonitorCheckResultRepository extends JpaRepository<MonitorCheckResult, Long> {

    List<MonitorCheckResult> findTop20ByMonitorOrderByCheckedAtDesc(Monitor monitor);

    Optional<MonitorCheckResult> findTopByMonitorOrderByCheckedAtDesc(Monitor monitor);

    long countByMonitorAndSuccessFalse(Monitor monitor);
}
