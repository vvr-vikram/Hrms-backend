package com.hrms.service;

import com.hrms.dto.EmployeeDTO;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.exception.DuplicateEmployeeException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.validator.EmployeeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private DepartmentRepository departmentRepository;
    
    @Mock
    private EmployeeValidator employeeValidator;
    
    @InjectMocks
    private EmployeeService employeeService;
    
    private EmployeeDTO employeeDTO;
    private Employee employee;
    private Department department;
    
    @BeforeEach
    void setUp() {
        department = Department.builder()
            .id(1L)
            .name("Engineering")
            .build();
        
        employeeDTO = EmployeeDTO.builder()
            .firstName("vikram")
            .lastName("v")
            .email("vikram.v@example.com")
            .phone("1234567890")
            .position("Software Engineer")
            .baseSalary(75000.0)
            .joiningDate(LocalDate.now())
            .departmentId(1L)
            .build();
        
        employee = Employee.builder()
            .id(1L)
            .employeeCode("EMP123")
            .firstName("bala")
            .lastName("b")
            .email("bala.b@example.com")
            .position("Software Engineer")
            .baseSalary(BigDecimal.valueOf(75000.0))
            .role("ROLE_EMPLOYEE")
            .department(department)
            .build();
    }
    
    @Test
    void onboardEmployee_Success() {
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(employeeRepository.existsByPhone(anyString())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        
        EmployeeDTO result = employeeService.onboardEmployee(employeeDTO);
        
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(employeeRepository).save(any(Employee.class));
    }
    
    @Test
    void onboardEmployee_DuplicateEmail_ThrowsException() {
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(true);
        
        assertThatThrownBy(() -> employeeService.onboardEmployee(employeeDTO))
            .isInstanceOf(DuplicateEmployeeException.class)
            .hasMessageContaining("already exists");
        
        verify(employeeRepository, never()).save(any(Employee.class));
    }
    
    @Test
    void getEmployeeById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        
        EmployeeDTO result = employeeService.getEmployeeById(1L);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
    }
    
    @Test
    void getEmployeeById_NotFound_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("not found");
    }
    
    @Test
    void updateEmployee_Success() {
        employeeDTO.setFirstName("Jane");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        
        EmployeeDTO result = employeeService.updateEmployee(1L, employeeDTO);
        
        assertThat(result).isNotNull();
        verify(employeeRepository).save(any(Employee.class));
    }
}