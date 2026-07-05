package com.erasm.controller;

import com.erasm.dto.EmployeeSkillRequest;
import com.erasm.dto.EmployeeSkillResponse;
import com.erasm.service.EmployeeSkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dedicated CRUD controller for Employee Skills (Module 3: Employee Skill
 * Profile / the Employee<->Skill Many-to-Many relationship). These
 * endpoints are independent of, and in addition to, the nested skill
 * endpoints under /api/employees/{id}/skills.
 */
@RestController
@RequestMapping("/api/employee-skills")
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;

    public EmployeeSkillController(EmployeeSkillService employeeSkillService) {
        this.employeeSkillService = employeeSkillService;
    }

    @PostMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<EmployeeSkillResponse> addSkill(@PathVariable Long employeeId,
                                                           @Valid @RequestBody EmployeeSkillRequest request) {
        return new ResponseEntity<>(employeeSkillService.addEmployeeSkill(employeeId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{employeeSkillId}/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<EmployeeSkillResponse> updateSkill(@PathVariable Long employeeId,
                                                              @PathVariable Long employeeSkillId,
                                                              @Valid @RequestBody EmployeeSkillRequest request) {
        return ResponseEntity.ok(employeeSkillService.updateEmployeeSkill(employeeId, employeeSkillId, request));
    }

    @DeleteMapping("/{employeeSkillId}/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RESOURCE_MANAGER')")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long employeeId, @PathVariable Long employeeSkillId) {
        employeeSkillService.deleteEmployeeSkill(employeeId, employeeSkillId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeSkillResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeSkillService.getSkillsByEmployee(employeeId));
    }

    @GetMapping("/{employeeSkillId}")
    public ResponseEntity<EmployeeSkillResponse> getById(@PathVariable Long employeeSkillId) {
        return ResponseEntity.ok(employeeSkillService.getEmployeeSkillById(employeeSkillId));
    }
}
