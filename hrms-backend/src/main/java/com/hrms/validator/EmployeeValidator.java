package com.hrms.validator;

import com.hrms.dto.EmployeeDTO;
import com.hrms.exception.DuplicateEmployeeException;
import com.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeValidator {

    private final EmployeeRepository employeeRepository;

    public void validateForOnboarding(EmployeeDTO employeeDTO) {
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateEmployeeException("Employee with email " + employeeDTO.getEmail() + " already exists");
        }
        
        if (employeeDTO.getPhone() != null && employeeRepository.existsByPhone(employeeDTO.getPhone())) {
            throw new DuplicateEmployeeException("Employee with phone " + employeeDTO.getPhone() + " already exists");
        }
    }
}