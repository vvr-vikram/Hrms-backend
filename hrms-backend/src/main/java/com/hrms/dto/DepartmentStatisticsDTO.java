package com.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatisticsDTO {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long totalEmployees;
    private Double totalMonthlySalary;
    private Double averageSalary;
}