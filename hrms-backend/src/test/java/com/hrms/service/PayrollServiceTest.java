package com.hrms.service;

import com.hrms.dto.PayrollDTO;
import com.hrms.entity.Employee;
import com.hrms.entity.Payroll;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRepository;
import com.hrms.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private PayrollService payrollService;

    private Employee employee;
    private Payroll payroll;
    private PayrollDTO payrollDTO;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("vikram")
                .lastName("v")
                .email("vikram.v@example.com")
                .position("Software Engineer")
                .baseSalary(BigDecimal.valueOf(75000.0))
                .role("ROLE_EMPLOYEE")
                .joiningDate(LocalDate.of(2023, 1, 15))
                .isActive(true)
                .build();

        payroll = Payroll.builder()
                .id(1L)
                .employee(employee)
                .year(2024)
                .month(1)
                .basicSalary(BigDecimal.valueOf(75000.0))
                .houseRentAllowance(BigDecimal.valueOf(18750.0))
                .dearnessAllowance(BigDecimal.valueOf(7500.0))
                .travelAllowance(BigDecimal.valueOf(3750.0))
                .medicalAllowance(BigDecimal.valueOf(2250.0))
                .specialAllowance(BigDecimal.valueOf(7500.0))
                .totalEarnings(BigDecimal.valueOf(114750.0))
                .providentFund(BigDecimal.valueOf(9000.0))
                .professionalTax(BigDecimal.valueOf(200.0))
                .incomeTax(BigDecimal.valueOf(7500.0))
                .loanDeduction(BigDecimal.valueOf(0.0))
                .totalDeductions(BigDecimal.valueOf(16700.0))
                .netSalary(BigDecimal.valueOf(98050.0))
                .totalPresentDays(22)
                .totalAbsentDays(1)
                .totalLeaveDays(0)
                .generatedAt(LocalDateTime.now())
                .generatedBy("SYSTEM")
                .build();

        payrollDTO = PayrollDTO.builder()
                .employeeId(1L)
                .year(2024)
                .month(1)
                .basicSalary(75000.0)
                .build();
    }

    @Test
    void generatePayroll_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployeeIdAndYearAndMonth(1L, 2024, 1))
                .thenReturn(false);
        when(attendanceRepository.countPresentDays(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(22L);
        // FIXED: Use the correct method name - countApprovedLeavesByEmployeeAndYear
        when(leaveRepository.countApprovedLeavesByEmployeeAndYear(eq(1L), eq(2024)))
                .thenReturn(0L);
        when(payrollRepository.save(any(Payroll.class))).thenReturn(payroll);

        PayrollDTO result = payrollService.generatePayroll(1L, 2024, 1);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeId()).isEqualTo(1L);
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getMonth()).isEqualTo(1);
        assertThat(result.getBasicSalary()).isEqualTo(75000.0);
        assertThat(result.getNetSalary()).isNotNull();
    }

    @Test
    void generatePayroll_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.generatePayroll(999L, 2024, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void generatePayroll_AlreadyExists_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployeeIdAndYearAndMonth(1L, 2024, 1))
                .thenReturn(true);

        assertThatThrownBy(() -> payrollService.generatePayroll(1L, 2024, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payroll already generated");
    }

    @Test
    void getEmployeePayrolls_Success() {
        when(payrollRepository.findByEmployeeId(1L)).thenReturn(java.util.List.of(payroll));

        var result = payrollService.getEmployeePayrolls(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeId()).isEqualTo(1L);
    }

    @Test
    void getPayrollByMonth_Success() {
        when(payrollRepository.findByEmployeeIdAndYearAndMonth(1L, 2024, 1))
                .thenReturn(Optional.of(payroll));

        PayrollDTO result = payrollService.getPayrollByMonth(1L, 2024, 1);

        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getMonth()).isEqualTo(1);
    }

    @Test
    void getPayrollByMonth_NotFound_ThrowsException() {
        when(payrollRepository.findByEmployeeIdAndYearAndMonth(999L, 2024, 1))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.getPayrollByMonth(999L, 2024, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payroll not found");
    }

    // Additional test for leave days calculation
    @Test
    void generatePayroll_WithLeaveDays_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployeeIdAndYearAndMonth(1L, 2024, 1))
                .thenReturn(false);
        when(attendanceRepository.countPresentDays(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(18L);
        // FIXED: Test with leave days
        when(leaveRepository.countApprovedLeavesByEmployeeAndYear(eq(1L), eq(2024)))
                .thenReturn(4L);
        when(payrollRepository.save(any(Payroll.class))).thenReturn(payroll);

        PayrollDTO result = payrollService.generatePayroll(1L, 2024, 1);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeId()).isEqualTo(1L);
    }
}