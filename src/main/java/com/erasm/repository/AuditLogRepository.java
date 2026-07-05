package com.erasm.repository;

import com.erasm.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityName(String entityName);
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);
    List<AuditLog> findByCreatedBy(String createdBy);
    List<AuditLog> findTop50ByOrderByIdDesc();
}
