package com.erasm.controller;

import com.erasm.dto.AuditLogResponse;
import com.erasm.service.AuditQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class AuditController {

    private final AuditQueryService auditQueryService;

    public AuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long entityId) {
        if (entityName != null && entityId != null) {
            return ResponseEntity.ok(auditQueryService.getAuditLogsByEntityAndId(entityName, entityId));
        }
        if (entityName != null) {
            return ResponseEntity.ok(auditQueryService.getAuditLogsByEntity(entityName));
        }
        return ResponseEntity.ok(auditQueryService.getAllAuditLogs());
    }

    /** Module 9 activity tracking endpoint: most recent activity across the system. */
    @GetMapping("/recent")
    public ResponseEntity<List<AuditLogResponse>> getRecentActivity() {
        return ResponseEntity.ok(auditQueryService.getRecentActivity());
    }

    /** Module 9 activity tracking endpoint: activity performed by a specific user. */
    @GetMapping("/user/{email}")
    public ResponseEntity<List<AuditLogResponse>> getActivityByUser(@PathVariable String email) {
        return ResponseEntity.ok(auditQueryService.getAuditLogsByUser(email));
    }
}
