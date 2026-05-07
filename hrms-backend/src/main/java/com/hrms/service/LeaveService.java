package com.hrms.service;

import com.hrms.dto.LeaveDTO;
import com.hrms.entity.Employee;
import com.hrms.entity.LeaveApplication;
import com.hrms.exception.InvalidLeaveException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveDTO applyForLeave(LeaveDTO leaveDTO) {
        log.info("Applying for leave for employee: {}", leaveDTO.getEmployeeId());
        
        Employee employee = employeeRepository.findById(leaveDTO.getEmployeeId())
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        // Validate leave dates
        validateLeaveDates(leaveDTO);
        
        // Check for overlapping approved leaves
        checkOverlappingLeaves(leaveDTO);
        
        // Calculate total days
        long totalDays = ChronoUnit.DAYS.between(leaveDTO.getStartDate(), leaveDTO.getEndDate()) + 1;
        leaveDTO.setTotalDays((int) totalDays);
        
        // Create leave request
        LeaveApplication leave = convertToEntity(leaveDTO);
        leave.setEmployee(employee);
        leave.setStatus("PENDING");
        
        LeaveApplication savedLeave = leaveRepository.save(leave);
        log.info("Leave request submitted with id: {}", savedLeave.getId());
        
        return convertToDTO(savedLeave);
    }

    @Transactional
    public LeaveDTO approveLeave(Long leaveId, String approvedBy) {
        log.info("Approving leave request: {} by {}", leaveId, approvedBy);
        
        LeaveApplication leave = leaveRepository.findById(leaveId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        if (!"PENDING".equals(leave.getStatus())) {
            throw new InvalidLeaveException("Leave request is already " + leave.getStatus());
        }
        
        leave.setStatus("APPROVED");
        leave.setApprovedAt(LocalDateTime.now());
        
        LeaveApplication updatedLeave = leaveRepository.save(leave);
        log.info("Leave request {} approved", leaveId);
        
        return convertToDTO(updatedLeave);
    }

    @Transactional
    public LeaveDTO rejectLeave(Long leaveId, String rejectedBy, String reason) {
        log.info("Rejecting leave request: {} by {}", leaveId, rejectedBy);
        
        LeaveApplication leave = leaveRepository.findById(leaveId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        leave.setStatus("REJECTED");
        leave.setRejectedAt(LocalDateTime.now());
        leave.setRejectionReason(reason);
        
        LeaveApplication updatedLeave = leaveRepository.save(leave);
        return convertToDTO(updatedLeave);
    }

    @Transactional
    public LeaveDTO cancelLeave(Long leaveId) {
        log.info("Cancelling leave request: {}", leaveId);
        
        LeaveApplication leave = leaveRepository.findById(leaveId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        if (!"PENDING".equals(leave.getStatus())) {
            throw new InvalidLeaveException("Cannot cancel leave that is already " + leave.getStatus());
        }
        
        leave.setStatus("CANCELLED");
        LeaveApplication updatedLeave = leaveRepository.save(leave);
        
        return convertToDTO(updatedLeave);
    }

    public List<LeaveDTO> getEmployeeLeaves(Long employeeId) {
        log.debug("Fetching leaves for employee: {}", employeeId);
        return leaveRepository.findByEmployeeId(employeeId)
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    public List<LeaveDTO> getPendingLeaves() {
        log.debug("Fetching all pending leave requests");
        return leaveRepository.findPendingLeavesOrderByDate()
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    private void validateLeaveDates(LeaveDTO leaveDTO) {
        LocalDate startDate = leaveDTO.getStartDate();
        LocalDate endDate = leaveDTO.getEndDate();
        
        if (startDate.isAfter(endDate)) {
            throw new InvalidLeaveException("Start date cannot be after end date");
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidLeaveException("Cannot apply for leave in the past");
        }
        
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (totalDays > 30) {
            throw new InvalidLeaveException("Cannot apply for more than 30 days of leave at once");
        }
    }

    private void checkOverlappingLeaves(LeaveDTO leaveDTO) {
        List<LeaveApplication> overlappingLeaves = leaveRepository.findByEmployeeIdAndStartDateBetweenAndStatus(
            leaveDTO.getEmployeeId(),
            leaveDTO.getStartDate(),
            leaveDTO.getEndDate(),
            "APPROVED"
        );
        
        if (!overlappingLeaves.isEmpty()) {
            throw new InvalidLeaveException("You already have an approved leave during this period");
        }
    }

    private LeaveApplication convertToEntity(LeaveDTO dto) {
        LeaveApplication leave = new LeaveApplication();
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setTotalDays(dto.getTotalDays());
        leave.setReason(dto.getReason());
        leave.setRemarks(dto.getComments());
        return leave;
    }

    private LeaveDTO convertToDTO(LeaveApplication entity) {
        return LeaveDTO.builder()
            .id(entity.getId())
            .employeeId(entity.getEmployee().getId())
            .employeeName(entity.getEmployee().getFullName())
            .employeeCode(entity.getEmployee().getEmployeeCode())
            .leaveType(entity.getLeaveType())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .totalDays(entity.getTotalDays())
            .reason(entity.getReason())
            .status(entity.getStatus())
            .approvedBy(entity.getApprovedBy() != null ? entity.getApprovedBy().getFullName() : null)
            .approvedDate(entity.getApprovedAt() != null ? entity.getApprovedAt().toLocalDate() : null)
            .rejectionReason(entity.getRejectionReason())
            .comments(entity.getRemarks())
            .build();
    }
}