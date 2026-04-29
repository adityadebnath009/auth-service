package com.aditya.simple_web_app.web_app.platform.monitoring.controller;


import com.aditya.simple_web_app.web_app.auth.Domain.User;
import com.aditya.simple_web_app.web_app.auth.service.CustomUserDetails;
import com.aditya.simple_web_app.web_app.platform.monitoring.domain.Monitor;
import com.aditya.simple_web_app.web_app.platform.monitoring.domain.MonitorCheckResult;
import com.aditya.simple_web_app.web_app.platform.monitoring.dto.CreateMonitorRequest;
import com.aditya.simple_web_app.web_app.platform.monitoring.dto.MonitorCheckResultResponse;
import com.aditya.simple_web_app.web_app.platform.monitoring.dto.MonitorResponse;
import com.aditya.simple_web_app.web_app.platform.monitoring.dto.PatchMonitorRequest;
import com.aditya.simple_web_app.web_app.platform.monitoring.service.MonitorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController(value = "/api")
public class MonitorController {

    private final MonitorService monitorService;
    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping("/monitors")
    public ResponseEntity<List<MonitorResponse>> getMonitors(@AuthenticationPrincipal CustomUserDetails customUserDetails)
    {
        User user = customUserDetails.getUser();

        List<MonitorResponse> monitorList = monitorService.getAllMonitorsForUser(user).stream()
                .map(monitor -> new MonitorResponse(
                        monitor.getId(),
                        monitor.getName(),
                        monitor.getUrl(),
                        monitor.getMethod(),
                        monitor.getExpectedStatus(),
                        monitor.getTimeoutMs(),
                        monitor.getIntervalSeconds(),

                        monitor.getCurrentStatus(),
                        monitor.isActive()
                ))
                .toList();
        return ResponseEntity.ok(monitorList);
    }

    @GetMapping("/monitors/{id}")
    public ResponseEntity<MonitorResponse> getMonitor(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails customUserDetails)
    {
        User user = customUserDetails.getUser();
        Monitor monitor = monitorService.getMonitorForUser(id, user);
        MonitorResponse monitorResponse = new MonitorResponse(
                monitor.getId(),
                monitor.getName(),
                monitor.getUrl(),
                monitor.getMethod(),
                monitor.getExpectedStatus(),
                monitor.getTimeoutMs(),
                monitor.getIntervalSeconds(),

                monitor.getCurrentStatus(),
                monitor.isActive()
        );
        return ResponseEntity.ok(monitorResponse);
    }

    @PostMapping("/monitors")
    public ResponseEntity createMonitor(@RequestBody @Valid CreateMonitorRequest request, @AuthenticationPrincipal CustomUserDetails customUserDetails)
    {
        Monitor monitor = monitorService.createMonitor(customUserDetails.getUser(), request);

        MonitorResponse monitorResponse = new MonitorResponse(
                monitor.getId(),
                monitor.getName(),
                monitor.getUrl(),
                monitor.getMethod(),
                monitor.getExpectedStatus(),
                monitor.getTimeoutMs(),
                monitor.getIntervalSeconds(),

                monitor.getCurrentStatus(),
                monitor.isActive()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(monitorResponse);
    }

    @DeleteMapping("/monitors/{id}")
    public ResponseEntity<Void> deleteMonitor(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails customUserDetails)
    {
        User user = customUserDetails.getUser();
        monitorService.deleteMonitor(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/monitors/{id}")
    public ResponseEntity<MonitorResponse> patchMonitor(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long id,
            @RequestBody @Validated PatchMonitorRequest request )
    {
        Monitor monitor = monitorService.patchMonitor(id, customUserDetails.getUser(), request);
        MonitorResponse monitorResponse = new MonitorResponse(
                monitor.getId(),
                monitor.getName(),
                monitor.getUrl(),
                monitor.getMethod(),
                monitor.getExpectedStatus(),
                monitor.getTimeoutMs(),
                monitor.getIntervalSeconds(),

                monitor.getCurrentStatus(),
                monitor.isActive()
        );


        return ResponseEntity.status(HttpStatus.OK).body(monitorResponse);



    }

    @PostMapping("/monitors/{id}/check")
    public ResponseEntity<MonitorCheckResultResponse> runCheck(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        User user = customUserDetails.getUser();
        MonitorCheckResult result = monitorService.runMonitorCheck(user, id);

        MonitorCheckResultResponse response = new MonitorCheckResultResponse(
                result.getId(),
                result.getCheckedAt(),
                result.getStatusCode(),
                result.getLatencyMs(),
                result.isSuccess(),
                result.getErrorMessage(),
                result.getMonitor().getId()
        );

        return ResponseEntity.ok(response);
    }





}
