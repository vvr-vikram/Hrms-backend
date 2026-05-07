package com.hrms.controller;

import com.hrms.entity.AuditLog;
import com.hrms.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Log Management", description = "APIs for viewing audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/entity/{entityName}/{entityId}")
    @Operation(summary = "Get audit logs by entity")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntity(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        log.info("GET /api/audit/entity/{}/{} - Fetching audit logs", entityName, entityId);
        List<AuditLog> logs = auditLogService.getAuditLogsByEntity(entityName, entityId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity/{entityName}/{entityId}/page")
    @Operation(summary = "Get audit logs by entity with pagination")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByEntityWithPagination(
            @PathVariable String entityName,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/audit/entity/{}/{}/page - Fetching audit logs with pagination", entityName, entityId);
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogService.getAuditLogsByEntity(entityName, entityId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get audit logs by action")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(@PathVariable String action) {
        log.info("GET /api/audit/action/{} - Fetching audit logs by action", action);
        List<AuditLog> logs = auditLogService.getAuditLogsByAction(action);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs by date range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/audit/date-range - Fetching audit logs from {} to {}", start, end);
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(start, end);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent audit logs")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs(@RequestParam(defaultValue = "24") int hours) {
        log.info("GET /api/audit/recent - Fetching recent audit logs for last {} hours", hours);
        List<AuditLog> logs = auditLogService.getRecentAuditLogs(hours);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get audit logs for employee")
    public ResponseEntity<List<AuditLog>> getAuditLogsForEmployee(@PathVariable Long employeeId) {
        log.info("GET /api/audit/employee/{} - Fetching audit logs for employee", employeeId);
        List<AuditLog> logs = auditLogService.getAuditLogsForEmployee(employeeId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/employee/{employeeId}/page")
    @Operation(summary = "Get audit logs for employee with pagination")
    public ResponseEntity<Page<AuditLog>> getAuditLogsForEmployeeWithPagination(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/audit/employee/{}/page - Fetching audit logs with pagination", employeeId);
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogService.getAuditLogsForEmployee(employeeId, pageable);
        return ResponseEntity.ok(logs);
    }
}