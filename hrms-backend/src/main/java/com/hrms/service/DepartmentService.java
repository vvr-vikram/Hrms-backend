package com.hrms.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrms.dto.DepartmentRequestDTO;
import com.hrms.dto.DepartmentStatisticsDTO;
import com.hrms.dto.response.DepartmentResponseDTO;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.mapper.DepartmentMapper;
import com.hrms.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO requestDTO) {
        log.info("Creating new department: {}", requestDTO.getName());
        
        if (departmentRepository.existsByName(requestDTO.getName())) {
            throw new DuplicateResourceException("Department with name '" + requestDTO.getName() + "' already exists");
        }
        
        if (departmentRepository.existsByDepartmentCode(requestDTO.getDepartmentCode())) {
            throw new DuplicateResourceException("Department with code '" + requestDTO.getDepartmentCode() + "' already exists");
        }
        
        Department department = departmentMapper.toEntity(requestDTO);
        Department savedDepartment = departmentRepository.save(department);
        log.info("Department created successfully with id: {}", savedDepartment.getId());
        
        return departmentMapper.toResponseDTO(savedDepartment);
    }

    @Cacheable(value = "departments", key = "#id")
    public DepartmentResponseDTO getDepartmentById(Long id) {
        log.debug("Fetching department with id: {}", id);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        
        return departmentMapper.toResponseDTO(department);
    }
    
    public DepartmentResponseDTO getDepartmentByIdWithEmployees(Long id) {
        log.debug("Fetching department with employees for id: {}", id);
        
        Department department = departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        
        return departmentMapper.toResponseDTO(department);
    }

    public List<DepartmentResponseDTO> getAllDepartments() {
        log.debug("Fetching all departments");
        
        List<Department> departments = departmentRepository.findAll();
        return departmentMapper.toResponseDTOList(departments);
    }
    
    public List<DepartmentResponseDTO> getActiveDepartments() {
        log.debug("Fetching all active departments");
        
        List<Department> departments = departmentRepository.findByIsActive(true);
        return departmentMapper.toResponseDTOList(departments);
    }

    @CacheEvict(value = "departments", key = "#id")
    @Transactional
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO requestDTO) {
        log.info("Updating department with id: {}", id);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        
        if (!department.getName().equals(requestDTO.getName()) && 
            departmentRepository.existsByName(requestDTO.getName())) {
            throw new DuplicateResourceException("Department with name '" + requestDTO.getName() + "' already exists");
        }
        
        if (!department.getDepartmentCode().equals(requestDTO.getDepartmentCode()) && 
            departmentRepository.existsByDepartmentCode(requestDTO.getDepartmentCode())) {
            throw new DuplicateResourceException("Department with code '" + requestDTO.getDepartmentCode() + "' already exists");
        }
        
        departmentMapper.updateEntity(requestDTO, department);
        Department updatedDepartment = departmentRepository.save(department);
        log.info("Department updated successfully with id: {}", updatedDepartment.getId());
        
        return departmentMapper.toResponseDTO(updatedDepartment);
    }

    @CacheEvict(value = "departments", key = "#id")
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department with id: {}", id);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        
        department.setIsActive(false);
        departmentRepository.save(department);
        log.info("Department deactivated successfully with id: {}", id);
    }
    
    @Transactional
    public void hardDeleteDepartment(Long id) {
        log.info("Hard deleting department with id: {}", id);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        
        departmentRepository.delete(department);
        log.info("Department hard deleted successfully with id: {}", id);
    }
    
    public List<DepartmentResponseDTO.EmployeeSummaryDTO> getDepartmentEmployees(Long departmentId) {
        log.debug("Fetching employees for department: {}", departmentId);
        
        Department department = departmentRepository.findByIdWithEmployees(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        
        DepartmentResponseDTO responseDTO = departmentMapper.toResponseDTO(department);
        return responseDTO.getEmployees() != null ? responseDTO.getEmployees() : new ArrayList<>();
    }
    
    // FIXED: Complete method with proper BigDecimal handling
    public List<DepartmentStatisticsDTO> getDepartmentStatistics() {
        log.debug("Fetching department statistics");
        
        List<Object[]> stats = departmentRepository.getDepartmentSalaryStats();
        List<DepartmentStatisticsDTO> statistics = new ArrayList<>();
        
        for (Object[] row : stats) {
            try {
                // Safely extract each value with null checks
                Long departmentId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String departmentName = row[1] != null ? row[1].toString() : "Unknown";
                String departmentCode = row[2] != null ? row[2].toString() : "N/A";
                Long totalEmployees = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                
                // Handle BigDecimal to Double conversion safely
                Double totalMonthlySalary = 0.0;
                if (row[4] != null) {
                    if (row[4] instanceof BigDecimal) {
                        totalMonthlySalary = ((BigDecimal) row[4]).doubleValue();
                    } else if (row[4] instanceof Double) {
                        totalMonthlySalary = (Double) row[4];
                    } else if (row[4] instanceof Number) {
                        totalMonthlySalary = ((Number) row[4]).doubleValue();
                    }
                }
                
                Double averageSalary = 0.0;
                if (row[5] != null) {
                    if (row[5] instanceof BigDecimal) {
                        averageSalary = ((BigDecimal) row[5]).doubleValue();
                    } else if (row[5] instanceof Double) {
                        averageSalary = (Double) row[5];
                    } else if (row[5] instanceof Number) {
                        averageSalary = ((Number) row[5]).doubleValue();
                    }
                }
                
                DepartmentStatisticsDTO dto = DepartmentStatisticsDTO.builder()
                        .departmentId(departmentId)
                        .departmentName(departmentName)
                        .departmentCode(departmentCode)
                        .totalEmployees(totalEmployees)
                        .totalMonthlySalary(totalMonthlySalary)
                        .averageSalary(averageSalary)
                        .build();
                
                statistics.add(dto);
                log.debug("Added statistics for department: {} - Employees: {}, Total Salary: {}", 
                         departmentName, totalEmployees, totalMonthlySalary);
                         
            } catch (Exception e) {
                log.error("Error processing department statistics row: {}", e.getMessage(), e);
            }
        }
        
        return statistics;
    }
    
    public long getDepartmentEmployeeCount(Long departmentId) {
        log.debug("Getting employee count for department: {}", departmentId);
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        
        return department.getEmployees().stream().filter(Employee::getIsActive).count();
    }
}