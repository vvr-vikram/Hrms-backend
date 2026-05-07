package com.hrms.mapper;

import com.hrms.dto.DepartmentRequestDTO;
import com.hrms.dto.response.DepartmentResponseDTO;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequestDTO dto) {
        if (dto == null) return null;
        
        return Department.builder()
                .name(dto.getName())
                .departmentCode(dto.getDepartmentCode())
                .description(dto.getDescription())
                .isActive(true)
                .build();
    }

    public DepartmentResponseDTO toResponseDTO(Department entity) {
        if (entity == null) return null;
        
        return DepartmentResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .departmentCode(entity.getDepartmentCode())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .employees(entity.getEmployees() != null ? 
                    entity.getEmployees().stream()
                        .map(this::toEmployeeSummary)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public List<DepartmentResponseDTO> toResponseDTOList(List<Department> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateEntity(DepartmentRequestDTO dto, Department entity) {
        if (dto == null || entity == null) return;
        
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDepartmentCode() != null) entity.setDepartmentCode(dto.getDepartmentCode());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
    }

    private DepartmentResponseDTO.EmployeeSummaryDTO toEmployeeSummary(Employee employee) {
        if (employee == null) return null;
        
        return DepartmentResponseDTO.EmployeeSummaryDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .position(employee.getPosition())
                .email(employee.getEmail())
                .build();
    }
}