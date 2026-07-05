package com.erasm.controller;

import com.erasm.dto.UtilizationResponse;
import com.erasm.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/utilization")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER', 'RESOURCE_MANAGER', 'AUDITOR')")
    public ResponseEntity<List<UtilizationResponse>> getUtilizationForAll() {
        return ResponseEntity.ok(dashboardService.getUtilizationForAllEmployees());
    }

    @GetMapping("/utilization/{employeeId}")
    public ResponseEntity<UtilizationResponse> getUtilizationForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(dashboardService.getUtilizationForEmployee(employeeId));
    }
}
