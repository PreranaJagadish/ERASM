package com.erasm.controller;

import com.erasm.dto.*;
import com.erasm.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESOURCE_MANAGER')")
    public ResponseEntity<EmployeeResponse> createEmployeeProfile(@PathVariable Long userId,
                                                                   @RequestBody EmployeeRequest request) {
        return new ResponseEntity<>(employeeService.createEmployeeProfile(userId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER', 'RESOURCE_MANAGER', 'AUDITOR')")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESOURCE_MANAGER')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {

        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {

        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Module 3: Employee Skill Profile ----

    @PostMapping("/{employeeId}/skills")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<EmployeeSkillResponse> addSkill(@PathVariable Long employeeId,
                                                           @Valid @RequestBody EmployeeSkillRequest request) {
        return new ResponseEntity<>(employeeService.addSkillToEmployee(employeeId, request), HttpStatus.CREATED);
    }
    

    @PutMapping("/{employeeId}/skills/{employeeSkillId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<EmployeeSkillResponse> updateSkillLevel(@PathVariable Long employeeId,
                                                                   @PathVariable Long employeeSkillId,
                                                                   @Valid @RequestBody EmployeeSkillRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployeeSkillLevel(employeeId, employeeSkillId, request));
    }

    @GetMapping("/{employeeId}/skills")
    public ResponseEntity<List<EmployeeSkillResponse>> getEmployeeSkills(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeService.getEmployeeSkills(employeeId));
    }

    @PostMapping("/{employeeId}/certifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<CertificationResponse> addCertification(@PathVariable Long employeeId,
                                                                    @Valid @RequestBody CertificationRequest request) {
        return new ResponseEntity<>(employeeService.addCertification(employeeId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{employeeId}/certifications")
    public ResponseEntity<List<CertificationResponse>> getCertifications(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeService.getEmployeeCertifications(employeeId));
    }
}
