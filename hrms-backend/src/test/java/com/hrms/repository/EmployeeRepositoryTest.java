package com.hrms.repository;

import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department department;
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @BeforeEach
    void setUp() {
        // Create department
        department = Department.builder()
                .name("Engineering")
                .departmentCode("ENG")
                .description("Software Engineering Department")
                .isActive(true)
                .build();
        entityManager.persist(department);

        // Create employees
        employee1 = Employee.builder()
                .employeeCode("EMP001")
                .firstName("vikram")
                .lastName("V")
                .email("vikram.v@example.com")
                .phone("1234567890")
                .position("Software Engineer")
                .baseSalary(BigDecimal.valueOf(75000.0))
                .joiningDate(LocalDate.of(2023, 1, 15))
                .role("ROLE_EMPLOYEE")
                .department(department)
                .isActive(true)
                .build();
        entityManager.persist(employee1);

        employee2 = Employee.builder()
                .employeeCode("EMP002")
                .firstName("b")
                .lastName("Smith")
                .email("bala.b@example.com")
                .phone("0987654321")
                .position("Senior Engineer")
                .baseSalary(BigDecimal.valueOf(95000.0))
                .joiningDate(LocalDate.of(2022, 6, 1))
                .role("ROLE_EMPLOYEE")
                .department(department)
                .isActive(true)
                .build();
        entityManager.persist(employee2);

        employee3 = Employee.builder()
                .employeeCode("EMP003")
                .firstName("dharun")
                .lastName("d")
                .email("dharun.d@example.com")
                .phone("5555555555")
                .position("Product Manager")
                .baseSalary(BigDecimal.valueOf(85000.0))
                .joiningDate(LocalDate.of(2023, 3, 10))
                .role("ROLE_MANAGER")
                .department(department)
                .isActive(true)
                .build();
        entityManager.persist(employee3);

        entityManager.flush();
    }

    @Test
    void findByEmployeeCode_Success() {
        Optional<Employee> found = employeeRepository.findByEmployeeCode("EMP001");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findByEmployeeCode_NotFound_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByEmployeeCode("EMP999");

        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_Success() {
        Optional<Employee> found = employeeRepository.findByEmail("jane.smith@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Jane");
        assertThat(found.get().getEmployeeCode()).isEqualTo("EMP002");
    }

    @Test
    void findByEmail_NotFound_ReturnsEmpty() {
        Optional<Employee> found = employeeRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void findByPhone_Success() {
        Optional<Employee> found = employeeRepository.findByPhone("5555555555");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Bob");
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        boolean exists = employeeRepository.existsByEmail("john.doe@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ReturnsFalse() {
        boolean exists = employeeRepository.existsByEmail("new@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByPhone_ReturnsTrue() {
        boolean exists = employeeRepository.existsByPhone("1234567890");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmployeeCode_ReturnsTrue() {
        boolean exists = employeeRepository.existsByEmployeeCode("EMP001");

        assertThat(exists).isTrue();
    }

    @Test
    void findByDepartmentId_Success() {
        List<Employee> employees = employeeRepository.findByDepartmentId(department.getId());

        assertThat(employees).isNotNull();
        assertThat(employees).hasSize(3);
        assertThat(employees).extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane", "Bob");
    }

    @Test
    void findByDepartmentId_NoEmployees_ReturnsEmptyList() {
        Department newDept = Department.builder()
                .name("HR")
                .departmentCode("HR")
                .build();
        entityManager.persist(newDept);
        entityManager.flush();

        List<Employee> employees = employeeRepository.findByDepartmentId(newDept.getId());

        assertThat(employees).isEmpty();
    }

    @Test
    void findByRole_Success() {
        List<Employee> employees = employeeRepository.findByRole("ROLE_EMPLOYEE");

        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void findByIsActive_Success() {
        // Deactivate one employee
        employee3.setIsActive(false);
        entityManager.persist(employee3);
        entityManager.flush();

        List<Employee> activeEmployees = employeeRepository.findByIsActive(true);
        List<Employee> inactiveEmployees = employeeRepository.findByIsActive(false);

        assertThat(activeEmployees).hasSize(2);
        assertThat(inactiveEmployees).hasSize(1);
        assertThat(inactiveEmployees.get(0).getFirstName()).isEqualTo("Bob");
    }

    @Test
    void findEmployeesJoinedBetween_Success() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        List<Employee> employees = employeeRepository.findEmployeesJoinedBetween(startDate, endDate);

        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("John", "Bob");
    }

    @Test
    void searchEmployees_ByFirstName_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.searchEmployees("John", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void searchEmployees_ByLastName_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.searchEmployees("Smith", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLastName()).isEqualTo("Smith");
    }

    @Test
    void searchEmployees_ByEmail_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.searchEmployees("jane.smith", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void searchEmployees_ByEmployeeCode_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.searchEmployees("EMP003", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmployeeCode()).isEqualTo("EMP003");
    }

    @Test
    void searchEmployees_NoMatch_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.searchEmployees("Nonexistent", pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void countActiveEmployeesByDepartment_Success() {
        Long count = employeeRepository.countActiveEmployeesByDepartment(department.getId());

        assertThat(count).isEqualTo(3);
    }

    @Test
    void countActiveEmployeesByDepartment_WithInactiveEmployees_Success() {
        employee3.setIsActive(false);
        entityManager.persist(employee3);
        entityManager.flush();

        Long count = employeeRepository.countActiveEmployeesByDepartment(department.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findAll_Pagination_Success() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Employee> page = employeeRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void save_Employee_Success() {
        Employee newEmployee = Employee.builder()
                .employeeCode("EMP004")
                .firstName("Alice")
                .lastName("Wonder")
                .email("alice@example.com")
                .phone("1112223333")
                .position("Designer")
                .baseSalary(BigDecimal.valueOf(70000.0))
                .joiningDate(LocalDate.now())
                .role("ROLE_EMPLOYEE")
                .department(department)
                .isActive(true)
                .build();

        Employee saved = employeeRepository.save(newEmployee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmployeeCode()).isEqualTo("EMP004");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");

        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void update_Employee_Success() {
        Employee employee = employeeRepository.findByEmployeeCode("EMP001").orElseThrow();
        employee.setPosition("Lead Software Engineer");
        employee.setBaseSalary(BigDecimal.valueOf(85000.0));

        Employee updated = employeeRepository.save(employee);

        assertThat(updated.getPosition()).isEqualTo("Lead Software Engineer");
        assertThat(updated.getBaseSalary()).isEqualTo(BigDecimal.valueOf(85000.0));

        Optional<Employee> found = employeeRepository.findById(employee.getId());
        assertThat(found.get().getPosition()).isEqualTo("Lead Software Engineer");
    }

    @Test
    void delete_Employee_Success() {
        Employee employee = employeeRepository.findByEmployeeCode("EMP001").orElseThrow();
        employeeRepository.delete(employee);

        Optional<Employee> found = employeeRepository.findById(employee.getId());
        assertThat(found).isEmpty();
    }
}