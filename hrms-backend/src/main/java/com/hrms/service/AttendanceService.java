package com.hrms.service;

import com.hrms.dto.AttendanceDTO;
import com.hrms.entity.Attendance;
import com.hrms.entity.Employee;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    private static final double STANDARD_WORKING_HOURS = 8.0;

    public AttendanceDTO markCheckIn(Long employeeId) {
        log.info("Marking check-in for employee: {}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        LocalDate today = LocalDate.now();
        
        // Check if attendance already exists for today
        Attendance existingAttendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
            .orElse(null);
        
        if (existingAttendance != null && existingAttendance.getCheckInTime() != null) {
            throw new RuntimeException("Check-in already marked for today");
        }
        
        Attendance attendance;
        if (existingAttendance == null) {
            attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setDate(today);
        } else {
            attendance = existingAttendance;
        }
        
        LocalDateTime checkInTime = LocalDateTime.now();
        attendance.setCheckInTime(checkInTime);
        
        // Determine status based on check-in time
        if (checkInTime.getHour() < 9) {
            attendance.setStatus("PRESENT");
        } else if (checkInTime.getHour() < 10) {
            attendance.setStatus("LATE");
        } else {
            attendance.setStatus("HALF_DAY");
        }
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Check-in recorded for employee: {} at {}", employeeId, checkInTime);
        
        return convertToDTO(savedAttendance);
    }

    public AttendanceDTO markCheckOut(Long employeeId) {
        log.info("Marking check-out for employee: {}", employeeId);
        
        LocalDate today = LocalDate.now();
        
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
            .orElseThrow(() -> new RuntimeException("No check-in record found for today"));
        
        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Check-out already marked for today");
        }
        
        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.setCheckOutTime(checkOutTime);
        
        // Calculate total working hours
        if (attendance.getCheckInTime() != null) {
            Duration duration = Duration.between(attendance.getCheckInTime(), checkOutTime);
            double workingHours = duration.toHours() + duration.toMinutesPart() / 60.0;
            attendance.setTotalWorkingHours(workingHours);
            
            if (workingHours > STANDARD_WORKING_HOURS) {
                attendance.setOvertimeHours(workingHours - STANDARD_WORKING_HOURS);
            }
        }
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Check-out recorded for employee: {} at {}", employeeId, checkOutTime);
        
        return convertToDTO(savedAttendance);
    }

    public List<AttendanceDTO> getEmployeeAttendance(Long employeeId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching attendance for employee: {} from {} to {}", employeeId, startDate, endDate);
        
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate)
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    public AttendanceDTO updateAttendance(Long attendanceId, AttendanceDTO attendanceDTO) {
        log.info("Updating attendance record: {}", attendanceId);
        
        Attendance attendance = attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
        
        attendance.setStatus(attendanceDTO.getStatus());
        attendance.setRemarks(attendanceDTO.getRemarks());
        
        if (attendanceDTO.getCheckInTime() != null) {
            attendance.setCheckInTime(attendanceDTO.getCheckInTime());
        }
        
        if (attendanceDTO.getCheckOutTime() != null) {
            attendance.setCheckOutTime(attendanceDTO.getCheckOutTime());
        }
        
        if (attendanceDTO.getTotalWorkingHours() != null) {
            attendance.setTotalWorkingHours(attendanceDTO.getTotalWorkingHours());
        }
        
        if (attendanceDTO.getOvertimeHours() != null) {
            attendance.setOvertimeHours(attendanceDTO.getOvertimeHours());
        }
        
        Attendance updatedAttendance = attendanceRepository.save(attendance);
        return convertToDTO(updatedAttendance);
    }

    private AttendanceDTO convertToDTO(Attendance entity) {
        return AttendanceDTO.builder()
            .id(entity.getId())
            .employeeId(entity.getEmployee().getId())
            .employeeName(entity.getEmployee().getFullName())
            .date(entity.getDate())
            .checkInTime(entity.getCheckInTime())
            .checkOutTime(entity.getCheckOutTime())
            .totalWorkingHours(entity.getTotalWorkingHours())
            .overtimeHours(entity.getOvertimeHours())
            .status(entity.getStatus())
            .remarks(entity.getRemarks())
            .build();
    }
}