package com.hrms.service;

import com.hrms.entity.AuditLog;
import com.hrms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public List<AuditLog> getAuditLogsByEntity(String entityName, Long entityId) {
        log.debug("Fetching audit logs for entity: {} with id: {}", entityName, entityId);
        return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId);
    }

    public Page<AuditLog> getAuditLogsByEntity(String entityName, Long entityId, Pageable pageable) {
        log.debug("Fetching audit logs for entity: {} with id: {} with pagination", entityName, entityId);
        return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId, pageable);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        log.debug("Fetching audit logs for action: {}", action);
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching audit logs between {} and {}", start, end);
        return auditLogRepository.findByPerformedAtBetween(start, end);
    }

    public List<AuditLog> getRecentAuditLogs(int hours) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(hours);
        return auditLogRepository.findByPerformedAtBetween(start, end);
    }

    public Long countActionsByDateRange(String action, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.countByActionAndPerformedAtBetween(action, start, end);
    }
    
    // Additional methods for employee-related audit logs (if needed)
    public List<AuditLog> getAuditLogsForEmployee(Long employeeId) {
        log.debug("Fetching audit logs for employee: {}", employeeId);
        return auditLogRepository.findByEntityNameAndEntityId("Employee", employeeId);
    }
    
    public Page<AuditLog> getAuditLogsForEmployee(Long employeeId, Pageable pageable) {
        log.debug("Fetching audit logs for employee: {} with pagination", employeeId);
        return auditLogRepository.findByEntityNameAndEntityId("Employee", employeeId, pageable);
    }
}