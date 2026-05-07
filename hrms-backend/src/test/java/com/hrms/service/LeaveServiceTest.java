package com.hrms.service;

import com.hrms.dto.LeaveDTO;
import com.hrms.entity.Employee;
import com.hrms.entity.LeaveApplication;
import com.hrms.exception.InvalidLeaveException;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @InjectMocks
    private LeaveService leaveService;
    
    private LeaveDTO leaveDTO;
    private LeaveApplication leave;
    private Employee employee;
    
    @BeforeEach
    void setUp() {
        employee = Employee.builder()
            .id(1L)
            .firstName("vikram")
            .lastName("v")
            .build();
        
        leaveDTO = LeaveDTO.builder()
            .employeeId(1L)
            .leaveType("ANNUAL")
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(5))
            .reason("Vacation")
            .build();
        
        leave = LeaveApplication.builder()
            .id(1L)
            .employee(employee)
            .leaveType("ANNUAL")
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(5))
            .reason("Vacation")
            .status("PENDING")
            .build();
    }
    
    @Test
    void applyForLeave_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRepository.save(any(LeaveApplication.class))).thenReturn(leave);
        
        LeaveDTO result = leaveService.applyForLeave(leaveDTO);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }
    
    @Test
    void applyForLeave_PastDate_ThrowsException() {
        leaveDTO.setStartDate(LocalDate.now().minusDays(1));
        
        assertThatThrownBy(() -> leaveService.applyForLeave(leaveDTO))
            .isInstanceOf(InvalidLeaveException.class)
            .hasMessageContaining("Cannot apply for leave in the past");
    }
    
    @Test
    void approveLeave_Success() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(LeaveApplication.class))).thenReturn(leave);
        
        LeaveDTO result = leaveService.approveLeave(1L, "Manager");
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }
    
    @Test
    void approveLeave_AlreadyApproved_ThrowsException() {
        leave.setStatus("APPROVED");
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        
        assertThatThrownBy(() -> leaveService.approveLeave(1L, "Manager"))
            .isInstanceOf(InvalidLeaveException.class)
            .hasMessageContaining("already APPROVED");
    }
}