package com.hrms.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hrms.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);

    Page<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByPerformedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByActionAndPerformedAtBetween(String action, LocalDateTime start, LocalDateTime end);
}