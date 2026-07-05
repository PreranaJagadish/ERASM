package com.erasm.controller;

import com.erasm.dto.ResourceRequestCreateRequest;
import com.erasm.dto.ResourceRequestResponse;
import com.erasm.dto.WorkflowActionRequest;
import com.erasm.service.ResourceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Module 5: Resource Request Management
 * Module 6: Approval Workflow
 *
 * Workflow: DRAFT -> SUBMITTED -> UNDER_REVIEW -> APPROVED -> ALLOCATED -> COMPLETED
 * (REJECTED reachable from UNDER_REVIEW)
 */
@RestController
@RequestMapping("/api/resource-requests")
public class ResourceRequestController {

    private final ResourceRequestService resourceRequestService;

    public ResourceRequestController(ResourceRequestService resourceRequestService) {
        this.resourceRequestService = resourceRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER')")
    public ResponseEntity<ResourceRequestResponse> createResourceRequest(
            @Valid @RequestBody ResourceRequestCreateRequest request) {
        return new ResponseEntity<>(resourceRequestService.createResourceRequest(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER', 'RESOURCE_MANAGER', 'AUDITOR')")
    public ResponseEntity<List<ResourceRequestResponse>> getAll(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status) {
        if (projectId != null) {
            return ResponseEntity.ok(resourceRequestService.getByProject(projectId));
        }
        if (status != null) {
            return ResponseEntity.ok(resourceRequestService.getByStatus(status));
        }
        return ResponseEntity.ok(resourceRequestService.getAllResourceRequests());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER', 'RESOURCE_MANAGER', 'AUDITOR')")
    public ResponseEntity<ResourceRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceRequestService.getById(id));
    }

    // ---- Module 6: Approval Workflow transitions ----

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER')")
    public ResponseEntity<ResourceRequestResponse> submit(@PathVariable Long id,
                                                           @RequestBody(required = false) WorkflowActionRequest request) {
        return ResponseEntity.ok(resourceRequestService.transitionStatus(id, "SUBMITTED", remarks(request)));
    }

    @PutMapping("/{id}/start-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESOURCE_MANAGER')")
    public ResponseEntity<ResourceRequestResponse> startReview(@PathVariable Long id,
                                                                @RequestBody(required = false) WorkflowActionRequest request) {
        return ResponseEntity.ok(resourceRequestService.transitionStatus(id, "UNDER_REVIEW", remarks(request)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESOURCE_MANAGER')")
    public ResponseEntity<ResourceRequestResponse> approve(@PathVariable Long id,
                                                            @RequestBody(required = false) WorkflowActionRequest request) {
        return ResponseEntity.ok(resourceRequestService.transitionStatus(id, "APPROVED", remarks(request)));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESOURCE_MANAGER')")
    public ResponseEntity<ResourceRequestResponse> reject(@PathVariable Long id,
                                                           @RequestBody(required = false) WorkflowActionRequest request) {
        return ResponseEntity.ok(resourceRequestService.transitionStatus(id, "REJECTED", remarks(request)));
    }

    @PutMapping("/{id}/mark-allocated")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESOURCE_MANAGER')")
    public ResponseEntity<ResourceRequestResponse> markAllocated(@PathVariable Long id,
                                                                  @RequestBody(required = false) WorkflowActionRequest request) {
        return ResponseEntity.ok(resourceRequestService.transitionStatus(id, "ALLOCATED", remarks(request)));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_MANAGER', 'RESOURCE_MANAGER')")
    public ResponseEntity<ResourceRequestResponse> complete(@PathVariable Long id,
                                                             @RequestBody(required = false) WorkflowActionRequest request) {
        return ResponseEntity.ok(resourceRequestService.transitionStatus(id, "COMPLETED", remarks(request)));
    }

    private String remarks(WorkflowActionRequest request) {
        return request != null ? request.getRemarks() : null;
    }
}
