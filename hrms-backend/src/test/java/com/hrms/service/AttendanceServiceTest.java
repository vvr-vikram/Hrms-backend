package com.hrms.service;

import com.hrms.dto.AttendanceDTO;
import com.hrms.entity.Attendance;
import com.hrms.entity.Employee;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private Attendance attendance;
    private AttendanceDTO attendanceDTO;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("vikram")
                .lastName("v")
                .email("vikram.v@example.com")
                .build();

        attendance = Attendance.builder()
                .id(1L)
                .employee(employee)
                .date(LocalDate.now())
                .checkInTime(LocalDateTime.now().withHour(9).withMinute(0))
                .status("PRESENT")
                .totalWorkingHours(8.0)
                .build();

        attendanceDTO = AttendanceDTO.builder()
                .employeeId(1L)
                .date(LocalDate.now())
                .status("PRESENT")
                .remarks("On time")
                .build();
    }

    @Test
    void markCheckIn_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance savedAttendance = invocation.getArgument(0);
            savedAttendance.setId(1L);
            return savedAttendance;
        });

        AttendanceDTO result = attendanceService.markCheckIn(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeId()).isEqualTo(1L);
        assertThat(result.getStatus()).isNotNull();
        assertThat(result.getCheckInTime()).isNotNull();
    }

    @Test
    void markCheckIn_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.markCheckIn(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void markCheckIn_AlreadyCheckedIn_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> attendanceService.markCheckIn(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Check-in already marked");
    }

    @Test
    void markCheckOut_Success() {
        when(attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        AttendanceDTO result = attendanceService.markCheckOut(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCheckOutTime()).isNotNull();
    }

    @Test
    void markCheckOut_NoCheckIn_ThrowsException() {
        when(attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.markCheckOut(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No check-in record found");
    }

    @Test
    void markCheckOut_AlreadyCheckedOut_ThrowsException() {
        attendance.setCheckOutTime(LocalDateTime.now());
        when(attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> attendanceService.markCheckOut(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Check-out already marked");
    }

    @Test
    void getEmployeeAttendance_Success() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(attendanceRepository.findByEmployeeIdAndDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(attendance));

        List<AttendanceDTO> result = attendanceService.getEmployeeAttendance(1L, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeId()).isEqualTo(1L);
    }

    @Test
    void getEmployeeAttendance_EmptyList_ReturnsEmptyList() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(attendanceRepository.findByEmployeeIdAndDateBetween(999L, startDate, endDate))
                .thenReturn(List.of());

        List<AttendanceDTO> result = attendanceService.getEmployeeAttendance(999L, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void updateAttendance_Success() {
        attendanceDTO.setStatus("HALF_DAY");
        attendanceDTO.setRemarks("Left early for appointment");
        attendanceDTO.setCheckInTime(attendance.getCheckInTime());
        attendanceDTO.setCheckOutTime(LocalDateTime.now().withHour(13).withMinute(0));

        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        AttendanceDTO result = attendanceService.updateAttendance(1L, attendanceDTO);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("HALF_DAY");
        assertThat(result.getRemarks()).isEqualTo("Left early for appointment");
    }

    @Test
    void updateAttendance_NotFound_ThrowsException() {
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.updateAttendance(999L, attendanceDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attendance record not found");
    }
}