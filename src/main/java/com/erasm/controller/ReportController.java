package com.erasm.controller;

import com.erasm.dto.ProjectAllocationReportResponse;
import com.erasm.dto.SkillReportResponse;
import com.erasm.dto.UtilizationResponse;
import com.erasm.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER', 'RESOURCE_MANAGER', 'AUDITOR')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/skill-report")
    public ResponseEntity<List<SkillReportResponse>> getSkillReport(
            @RequestParam(required = false) String skillName) {
        return ResponseEntity.ok(reportService.getSkillReport(skillName));
    }

    @GetMapping("/utilization-report")
    public ResponseEntity<List<UtilizationResponse>> getUtilizationReport() {
        return ResponseEntity.ok(reportService.getUtilizationReport());
    }

    @GetMapping("/project-allocation-report")
    public ResponseEntity<List<ProjectAllocationReportResponse>> getProjectAllocationReport(
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(reportService.getProjectAllocationReport(projectId));
    }
}
