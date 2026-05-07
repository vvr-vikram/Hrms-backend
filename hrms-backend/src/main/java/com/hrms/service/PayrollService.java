package com.hrms.service;

import com.hrms.dto.PayrollDTO;
import com.hrms.entity.Employee;
import com.hrms.entity.Payroll;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRepository;
import com.hrms.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;

    private static final BigDecimal HRA_PERCENTAGE = BigDecimal.valueOf(0.25);
    private static final BigDecimal DA_PERCENTAGE = BigDecimal.valueOf(0.10);
    private static final BigDecimal TA_PERCENTAGE = BigDecimal.valueOf(0.05);
    private static final BigDecimal MA_PERCENTAGE = BigDecimal.valueOf(0.03);
    private static final BigDecimal PF_PERCENTAGE = BigDecimal.valueOf(0.12);
    private static final BigDecimal PT_AMOUNT = BigDecimal.valueOf(200.0);
    private static final BigDecimal LATE_DEDUCTION_RATE = BigDecimal.valueOf(50.0);
    private static final BigDecimal BONUS_PERCENTAGE_HIGH = BigDecimal.valueOf(0.15);
    private static final BigDecimal BONUS_PERCENTAGE_MEDIUM = BigDecimal.valueOf(0.10);
    private static final BigDecimal BONUS_PERCENTAGE_LOW = BigDecimal.valueOf(0.05);

    public PayrollDTO generatePayroll(Long employeeId, int year, int month) {
        log.info("Generating payroll for employee: {} for {}-{}", employeeId, year, month);
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        // Check if payroll already exists
        if (payrollRepository.existsByEmployeeIdAndYearAndMonth(employeeId, year, month)) {
            throw new RuntimeException("Payroll already generated for this month");
        }
        
        // Calculate attendance details
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        
        long presentDays = attendanceRepository.countPresentDays(employeeId, startDate, endDate);
        long totalWorkingDays = endDate.getDayOfMonth();
        long absentDays = totalWorkingDays - presentDays;
        
        // Get approved leaves for the year
        long leaveDays = leaveRepository.countApprovedLeavesByEmployeeAndYear(employeeId, year);
        
        // Calculate earnings
        BigDecimal basicSalary = employee.getBaseSalary();
        BigDecimal houseRentAllowance = basicSalary.multiply(HRA_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dearnessAllowance = basicSalary.multiply(DA_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal travelAllowance = basicSalary.multiply(TA_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal medicalAllowance = basicSalary.multiply(MA_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal specialAllowance = calculateSpecialAllowance(employee, presentDays, totalWorkingDays);
        
        BigDecimal totalEarnings = basicSalary
            .add(houseRentAllowance)
            .add(dearnessAllowance)
            .add(travelAllowance)
            .add(medicalAllowance)
            .add(specialAllowance);
        
        // Calculate deductions
        BigDecimal providentFund = basicSalary.multiply(PF_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal professionalTax = PT_AMOUNT;
        BigDecimal incomeTax = calculateIncomeTax(basicSalary);
        BigDecimal loanDeduction = calculateLoanDeduction(employeeId);
        BigDecimal lateDeduction = calculateLateDeductions(employeeId, startDate, endDate);
        
        BigDecimal totalDeductions = providentFund
            .add(professionalTax)
            .add(incomeTax)
            .add(loanDeduction)
            .add(lateDeduction);
        
        // Calculate net salary
        BigDecimal netSalary = totalEarnings.subtract(totalDeductions);
        
        // Create payroll record
        Payroll payroll = Payroll.builder()
            .employee(employee)
            .year(year)
            .month(month)
            .basicSalary(basicSalary)
            .houseRentAllowance(houseRentAllowance)
            .dearnessAllowance(dearnessAllowance)
            .travelAllowance(travelAllowance)
            .medicalAllowance(medicalAllowance)
            .specialAllowance(specialAllowance)
            .totalEarnings(totalEarnings)
            .providentFund(providentFund)
            .professionalTax(professionalTax)
            .incomeTax(incomeTax)
            .loanDeduction(loanDeduction)
            .totalDeductions(totalDeductions)
            .netSalary(netSalary)
            .totalPresentDays((int) presentDays)
            .totalAbsentDays((int) absentDays)
            .totalLeaveDays((int) leaveDays)
            .generatedAt(LocalDateTime.now())
            .generatedBy("SYSTEM")
            .paymentStatus("PENDING")
            .build();
        
        Payroll savedPayroll = payrollRepository.save(payroll);
        log.info("Payroll generated successfully for employee: {}", employeeId);
        
        return convertToDTO(savedPayroll);
    }

    public List<PayrollDTO> getEmployeePayrolls(Long employeeId) {
        log.debug("Fetching payrolls for employee: {}", employeeId);
        return payrollRepository.findByEmployeeId(employeeId)
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    public PayrollDTO getPayrollByMonth(Long employeeId, int year, int month) {
        Payroll payroll = payrollRepository.findByEmployeeIdAndYearAndMonth(employeeId, year, month)
            .orElseThrow(() -> new ResourceNotFoundException("Payroll not found for the specified month"));
        return convertToDTO(payroll);
    }

    public PayrollDTO updatePaymentStatus(Long payrollId, String status, String approvedBy) {
        log.info("Updating payment status for payroll: {} to {}", payrollId, status);
        
        Payroll payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));
        
        payroll.setPaymentStatus(status);
        if ("APPROVED".equals(status)) {
            payroll.setApprovedBy(approvedBy);
            payroll.setApprovedAt(LocalDateTime.now());
            payroll.setPaymentDate(LocalDate.now());
        }
        
        Payroll updatedPayroll = payrollRepository.save(payroll);
        return convertToDTO(updatedPayroll);
    }

    private BigDecimal calculateSpecialAllowance(Employee employee, long presentDays, long totalWorkingDays) {
        if (totalWorkingDays == 0) {
            return BigDecimal.ZERO;
        }
        
        double attendancePercentage = (double) presentDays / totalWorkingDays;
        BigDecimal bonusPercentage;
        
        if (attendancePercentage >= 0.95) {
            bonusPercentage = BONUS_PERCENTAGE_HIGH;
        } else if (attendancePercentage >= 0.90) {
            bonusPercentage = BONUS_PERCENTAGE_MEDIUM;
        } else if (attendancePercentage >= 0.85) {
            bonusPercentage = BONUS_PERCENTAGE_LOW;
        } else {
            bonusPercentage = BigDecimal.ZERO;
        }
        
        return employee.getBaseSalary()
            .multiply(bonusPercentage)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateIncomeTax(BigDecimal basicSalary) {
        BigDecimal annualSalary = basicSalary.multiply(BigDecimal.valueOf(12));
        double annualSalaryValue = annualSalary.doubleValue();
        double tax = 0;
        
        if (annualSalaryValue <= 250000) {
            tax = 0;
        } else if (annualSalaryValue <= 500000) {
            tax = (annualSalaryValue - 250000) * 0.05;
        } else if (annualSalaryValue <= 1000000) {
            tax = 12500 + (annualSalaryValue - 500000) * 0.20;
        } else {
            tax = 112500 + (annualSalaryValue - 1000000) * 0.30;
        }
        
        // Return monthly tax
        return BigDecimal.valueOf(tax / 12).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLoanDeduction(Long employeeId) {
        // Implement loan deduction logic based on active loans
        // This is a placeholder - implement based on your loan management system
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateLateDeductions(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Long lateDays = attendanceRepository.countLateDaysByEmployee(employeeId, startDate, endDate);
        if (lateDays == null || lateDays == 0) {
            return BigDecimal.ZERO;
        }
        return LATE_DEDUCTION_RATE.multiply(BigDecimal.valueOf(lateDays));
    }

    private PayrollDTO convertToDTO(Payroll entity) {
        return PayrollDTO.builder()
            .id(entity.getId())
            .employeeId(entity.getEmployee().getId())
            .employeeName(entity.getEmployee().getFullName())
            .employeeCode(entity.getEmployee().getEmployeeCode())
            .year(entity.getYear())
            .month(entity.getMonth())
            .basicSalary(entity.getBasicSalary() != null ? entity.getBasicSalary().doubleValue() : 0.0)
            .houseRentAllowance(entity.getHouseRentAllowance() != null ? entity.getHouseRentAllowance().doubleValue() : 0.0)
            .dearnessAllowance(entity.getDearnessAllowance() != null ? entity.getDearnessAllowance().doubleValue() : 0.0)
            .travelAllowance(entity.getTravelAllowance() != null ? entity.getTravelAllowance().doubleValue() : 0.0)
            .medicalAllowance(entity.getMedicalAllowance() != null ? entity.getMedicalAllowance().doubleValue() : 0.0)
            .specialAllowance(entity.getSpecialAllowance() != null ? entity.getSpecialAllowance().doubleValue() : 0.0)
            .totalEarnings(entity.getTotalEarnings() != null ? entity.getTotalEarnings().doubleValue() : 0.0)
            .providentFund(entity.getProvidentFund() != null ? entity.getProvidentFund().doubleValue() : 0.0)
            .professionalTax(entity.getProfessionalTax() != null ? entity.getProfessionalTax().doubleValue() : 0.0)
            .incomeTax(entity.getIncomeTax() != null ? entity.getIncomeTax().doubleValue() : 0.0)
            .loanDeduction(entity.getLoanDeduction() != null ? entity.getLoanDeduction().doubleValue() : 0.0)
            .totalDeductions(entity.getTotalDeductions() != null ? entity.getTotalDeductions().doubleValue() : 0.0)
            .netSalary(entity.getNetSalary() != null ? entity.getNetSalary().doubleValue() : 0.0)
            .totalPresentDays(entity.getTotalPresentDays())
            .totalAbsentDays(entity.getTotalAbsentDays())
            .totalLeaveDays(entity.getTotalLeaveDays())
            .paymentMode(entity.getPaymentMethod())
            .remarks(entity.getRemarks())
            .build();
    }
}