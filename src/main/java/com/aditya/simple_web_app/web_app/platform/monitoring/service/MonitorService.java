package com.aditya.simple_web_app.web_app.platform.monitoring.service;


import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.platform.monitoring.domain.Monitor;
import com.aditya.simple_web_app.web_app.platform.monitoring.dto.CreateMonitorRequest;
import com.aditya.simple_web_app.web_app.platform.monitoring.repository.MonitorRepository;
import org.springframework.stereotype.Service;

@Service
public class MonitorService {
    private final MonitorRepository monitorRepository;
    public MonitorService(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    public Monitor createMonitor(User user, CreateMonitorRequest request)
    {
        Monitor monitor = Monitor.builder()
                .name(request.name())
                .url(request.url())
                .currentStatus("UNKNOWN")
                .method(request.method())
                .expectedStatus(request.expectedStatus())
                .timeoutMs(request.timeoutMs()).
                intervalSeconds(request.intervalSeconds()).
                active(true).
                owner(user).
                build();

        monitorRepository.save(monitor);

        return monitor;
    }

}
