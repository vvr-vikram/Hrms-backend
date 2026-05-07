package com.hrms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hrms.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    Optional<Department> findByDepartmentCode(String departmentCode);

    boolean existsByName(String name);

    boolean existsByDepartmentCode(String departmentCode);

    List<Department> findByIsActive(Boolean isActive);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
    Optional<Department> findByIdWithEmployees(@Param("id") Long id);

    // FIXED: Explicitly cast BigDecimal to appropriate types
    @Query("SELECT d.id, d.name, d.departmentCode, " +
           "COUNT(e.id) as totalEmployees, " +
           "COALESCE(SUM(e.baseSalary), 0) as totalSalary, " +
           "COALESCE(AVG(e.baseSalary), 0) as avgSalary " +
           "FROM Department d " +
           "LEFT JOIN d.employees e " +
           "WHERE e.isActive = true OR e.isActive IS NULL " +
           "GROUP BY d.id, d.name, d.departmentCode")
    List<Object[]> getDepartmentSalaryStats();
}