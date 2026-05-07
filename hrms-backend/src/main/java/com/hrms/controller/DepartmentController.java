package com.hrms.controller;

import com.hrms.dto.DepartmentRequestDTO;
import com.hrms.dto.DepartmentStatisticsDTO;
import com.hrms.dto.response.DepartmentResponseDTO;
import com.hrms.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Department Management", description = "APIs for managing departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "Create a new department")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(@Valid @RequestBody DepartmentRequestDTO requestDTO) {
        log.info("POST /api/departments - Creating new department");
        DepartmentResponseDTO department = departmentService.createDepartment(requestDTO);
        return new ResponseEntity<>(department, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable Long id) {
        log.info("GET /api/departments/{} - Fetching department", id);
        DepartmentResponseDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @GetMapping("/{id}/with-employees")
    @Operation(summary = "Get department with employees")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentWithEmployees(@PathVariable Long id) {
        log.info("GET /api/departments/{}/with-employees - Fetching department with employees", id);
        DepartmentResponseDTO department = departmentService.getDepartmentByIdWithEmployees(id);
        return ResponseEntity.ok(department);
    }

    @GetMapping
    @Operation(summary = "Get all departments")
    public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments() {
        log.info("GET /api/departments - Fetching all departments");
        List<DepartmentResponseDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active departments")
    public ResponseEntity<List<DepartmentResponseDTO>> getActiveDepartments() {
        log.info("GET /api/departments/active - Fetching active departments");
        List<DepartmentResponseDTO> departments = departmentService.getActiveDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get department statistics")
    public ResponseEntity<List<DepartmentStatisticsDTO>> getDepartmentStatistics() {
        log.info("GET /api/departments/statistics - Fetching department statistics");
        List<DepartmentStatisticsDTO> statistics = departmentService.getDepartmentStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDTO requestDTO) {
        log.info("PUT /api/departments/{} - Updating department", id);
        DepartmentResponseDTO department = departmentService.updateDepartment(id, requestDTO);
        return ResponseEntity.ok(department);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete department")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        log.info("DELETE /api/departments/{} - Soft deleting department", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @Operation(summary = "Hard delete department")
    public ResponseEntity<Void> hardDeleteDepartment(@PathVariable Long id) {
        log.info("DELETE /api/departments/{}/hard - Hard deleting department", id);
        departmentService.hardDeleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/employees")
    @Operation(summary = "Get department employees")
    public ResponseEntity<List<DepartmentResponseDTO.EmployeeSummaryDTO>> getDepartmentEmployees(@PathVariable Long id) {
        log.info("GET /api/departments/{}/employees - Fetching department employees", id);
        List<DepartmentResponseDTO.EmployeeSummaryDTO> employees = departmentService.getDepartmentEmployees(id);
        return ResponseEntity.ok(employees);
    }
}