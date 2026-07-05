package com.erasm.service;

import com.erasm.dto.AuditLogResponse;
import com.erasm.entity.AuditLog;
import com.erasm.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    public AuditQueryService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<AuditLogResponse> getAuditLogsByEntity(String entityName) {
        return auditLogRepository.findByEntityName(entityName).stream().map(this::toResponse).toList();
    }

    public List<AuditLogResponse> getAuditLogsByEntityAndId(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId).stream()
                .map(this::toResponse).toList();
    }

    /** Module 9 activity tracking: most recent audit activity across the whole system. */
    public List<AuditLogResponse> getRecentActivity() {
        return auditLogRepository.findTop50ByOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    /** Module 9 activity tracking: audit activity performed by a specific user (by email). */
    public List<AuditLogResponse> getAuditLogsByUser(String performedBy) {
        return auditLogRepository.findByCreatedBy(performedBy).stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setEntityName(log.getEntityName());
        response.setEntityId(log.getEntityId());
        response.setAction(log.getAction());
        response.setDetails(log.getDetails());
        response.setCreatedBy(log.getCreatedBy());
        response.setCreatedDate(log.getCreatedDate());
        return response;
    }
}
