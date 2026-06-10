package com.cts.adstudio.mediaplanservice.repository;

import com.cts.adstudio.mediaplanservice.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Integer entityId);
    List<AuditLog> findByUserId(Integer userId);
}