package com.erasm.controller;

import com.erasm.dto.AllocationRequest;
import com.erasm.dto.AllocationResponse;
import com.erasm.service.AllocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESOURCE_MANAGER')")
    public ResponseEntity<AllocationResponse> allocateEmployee(@Valid @RequestBody AllocationRequest request) {
        return new ResponseEntity<>(allocationService.allocateEmployee(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESOURCE_MANAGER')")
    public ResponseEntity<AllocationResponse> reallocateEmployee(@PathVariable Long id,
                                                                   @Valid @RequestBody AllocationRequest request) {
        return ResponseEntity.ok(allocationService.reallocateEmployee(id, request));
    }

    @PutMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESOURCE_MANAGER')")
    public ResponseEntity<AllocationResponse> releaseEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(allocationService.releaseEmployee(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER', 'RESOURCE_MANAGER', 'AUDITOR')")
    public ResponseEntity<List<AllocationResponse>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long projectId) {
        if (employeeId != null) {
            return ResponseEntity.ok(allocationService.getAllocationsByEmployee(employeeId));
        }
        if (projectId != null) {
            return ResponseEntity.ok(allocationService.getAllocationsByProject(projectId));
        }
        return ResponseEntity.ok(allocationService.getAllAllocations());
    }
}
