package com.erasm.service;

import com.erasm.entity.AuditLog;
import com.erasm.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Records an audit trail entry for every critical action performed in the
 * system (Module 9: Audit Management). Every mutating service call in the
 * application should call {@link #log} after a successful operation.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String entityName, Long entityId, String action, String details, String performedBy) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setDetails(details);
        auditLog.setCreatedBy(performedBy);
        auditLog.setModifiedBy(performedBy);
        auditLog.setCreatedDate(LocalDateTime.now().toString());
        auditLog.setModifiedDate(LocalDateTime.now().toString());
        auditLogRepository.save(auditLog);
        logger.debug("Audit entry recorded: entity={}, id={}, action={}, by={}", entityName, entityId, action,
                performedBy);
    }
}
