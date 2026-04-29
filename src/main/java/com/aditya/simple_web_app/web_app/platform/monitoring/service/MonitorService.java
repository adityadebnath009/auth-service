package com.aditya.simple_web_app.web_app.platform.monitoring.service;


import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.auth.service.CustomUserDetails;
import com.aditya.simple_web_app.web_app.common.exception.ResourceNotFoundException;
import com.aditya.simple_web_app.web_app.config.WebClientConfig;
import com.aditya.simple_web_app.web_app.platform.monitoring.domain.Monitor;
import com.aditya.simple_web_app.web_app.platform.monitoring.domain.MonitorCheckResult;
import com.aditya.simple_web_app.web_app.platform.monitoring.dto.CreateMonitorRequest;
import com.aditya.simple_web_app.web_app.platform.monitoring.dto.PatchMonitorRequest;
import com.aditya.simple_web_app.web_app.platform.monitoring.repository.MonitorCheckResultRepository;
import com.aditya.simple_web_app.web_app.platform.monitoring.repository.MonitorRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MonitorService {
    private final MonitorRepository monitorRepository;
    private final WebClient.Builder webClient;
    private final MonitorCheckResultRepository monitorCheckResultRepository;
    private final RestTemplate restTemplate;
    public MonitorService(MonitorRepository monitorRepository, WebClient.Builder webClient, MonitorCheckResultRepository monitorCheckResultRepository, RestTemplate restTemplate) {
        this.monitorRepository = monitorRepository;
        this.webClient = webClient;
        this.monitorCheckResultRepository = monitorCheckResultRepository;
        this.restTemplate = restTemplate;
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

    public List<Monitor> getAllMonitorsForUser(User user)
    {
        List<Monitor> monitorList = monitorRepository.findAllByOwner(user);
        return monitorList;
    }
    public Monitor getMonitorForUser(Long monitorId, User user)
    {
        return monitorRepository.findByIdAndOwner(monitorId, user).orElseThrow(
                () -> new ResourceNotFoundException("Monitor not found")
        );
    }

    public void deleteMonitor(Long monitorId, User user)
    {
        Monitor monitor = getMonitorForUser(monitorId, user);
        monitorRepository.delete(monitor);
    }

    public Monitor patchMonitor(Long monitorId, User user, PatchMonitorRequest request)
    {
        Monitor existingMonitor = getMonitorForUser(monitorId, user);
        if (request.name() != null) {
            existingMonitor.setName(request.name());
        }
        if (request.url() != null) {
            existingMonitor.setUrl(request.url());
        }
        if (request.method() != null) {
            existingMonitor.setMethod(request.method());
        }
        if (request.expectedStatus() != null) {
            existingMonitor.setExpectedStatus(request.expectedStatus());
        }
        if (request.timeoutMs() != null) {
            existingMonitor.setTimeoutMs(request.timeoutMs());
        }
        if (request.intervalSeconds() != null) {
            existingMonitor.setIntervalSeconds(request.intervalSeconds());
        }
        if (request.active() != null) {
            existingMonitor.setActive(request.active());
        }

        return monitorRepository.save(existingMonitor);
    }

    public MonitorCheckResult runMonitorCheck(User user, Long monitorId)
    {

        Monitor monitor = monitorRepository.findByIdAndOwner(monitorId, user).get();

        return runMonitorCheck(monitor);

    }
    private MonitorCheckResult runMonitorCheck(Monitor monitor)
    {
        long startTime = System.currentTimeMillis();

        try{
            HttpMethod httpMethod = HttpMethod.valueOf(monitor.getMethod().toUpperCase());
            RequestEntity<Void> request = new RequestEntity<>(
                    httpMethod,
                    URI.create(monitor.getUrl())
            );

            ResponseEntity<String> response = restTemplate.exchange(request, String.class);




            int latency = (int) (System.currentTimeMillis() - startTime);
            int statusCode = response.getStatusCode().value();
            boolean success = statusCode == monitor.getExpectedStatus();

            MonitorCheckResult result = MonitorCheckResult.builder()
                    .checkedAt(Instant.now())
                    .statusCode(statusCode)
                    .latencyMs(latency)
                    .success(success)
                    .errorMessage(null)
                    .monitor(monitor)
                    .build();

            monitor.setCurrentStatus(success ? "UP" : "DOWN");
            monitorRepository.save(monitor);
            return monitorCheckResultRepository.save(result);




        }
        catch(HttpStatusCodeException ex)
        {
            int latency = (int) (System.currentTimeMillis() - startTime);
            int statusCode = ex.getStatusCode().value();
            boolean success = statusCode == monitor.getExpectedStatus();

            MonitorCheckResult result = MonitorCheckResult.builder()
                    .checkedAt(Instant.now())
                    .statusCode(statusCode)
                    .latencyMs(latency)
                    .success(success)
                    .errorMessage(ex.getStatusText())
                    .monitor(monitor)
                    .build();

            monitor.setCurrentStatus(success ? "UP" : "DOWN");
            monitorRepository.save(monitor);
            return monitorCheckResultRepository.save(result);
        }
        catch(Exception ex){


            int latency = (int) (System.currentTimeMillis() - startTime);

            MonitorCheckResult result = MonitorCheckResult.builder()
                    .checkedAt(Instant.now())
                    .statusCode(null)
                    .latencyMs(latency)
                    .success(false)
                    .errorMessage(ex.getMessage() != null ? ex.getMessage() : "Request failed")
                    .monitor(monitor)
                    .build();

            monitor.setCurrentStatus("DOWN");
            monitorRepository.save(monitor);
            return monitorCheckResultRepository.save(result);

        }

    }

}
