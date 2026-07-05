package com.erasm.controller;

import com.erasm.dto.CertificationRequest;
import com.erasm.dto.CertificationResponse;
import com.erasm.service.CertificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dedicated CRUD controller for Certifications (Module 3: Employee Skill
 * Profile). These endpoints are independent of, and in addition to, the
 * nested certification endpoints under /api/employees/{id}/certifications.
 */
@RestController
@RequestMapping("/api/certifications")
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<CertificationResponse> addCertification(@PathVariable Long employeeId,
                                                                    @Valid @RequestBody CertificationRequest request) {
        return new ResponseEntity<>(certificationService.addCertification(employeeId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{certificationId}/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<CertificationResponse> updateCertification(@PathVariable Long employeeId,
                                                                       @PathVariable Long certificationId,
                                                                       @Valid @RequestBody CertificationRequest request) {
        return ResponseEntity.ok(certificationService.updateCertification(employeeId, certificationId, request));
    }

    @DeleteMapping("/{certificationId}/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long employeeId, @PathVariable Long certificationId) {
        certificationService.deleteCertification(employeeId, certificationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<CertificationResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(certificationService.getCertificationsByEmployee(employeeId));
    }

    @GetMapping("/{certificationId}")
    public ResponseEntity<CertificationResponse> getById(@PathVariable Long certificationId) {
        return ResponseEntity.ok(certificationService.getCertificationById(certificationId));
    }
}
