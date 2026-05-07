-- =============================================
-- HRMS COMPLETE BACKEND SYSTEM
-- MySQL Database Setup Script (FIXED)
-- =============================================

-- Drop database if exists and recreate
DROP DATABASE IF EXISTS hrms_db;
CREATE DATABASE hrms_db;
USE hrms_db;

-- =============================================
-- 1. DEPARTMENT TABLE
-- =============================================
CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    department_code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dept_code (department_code),
    INDEX idx_dept_active (is_active)
);

-- =============================================
-- 2. EMPLOYEE TABLE (with history tracking)
-- =============================================
CREATE TABLE employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address TEXT,
    date_of_birth DATE,
    gender VARCHAR(10),
    position VARCHAR(100) NOT NULL,
    base_salary DECIMAL(12, 2) NOT NULL,
    joining_date DATE NOT NULL,
    role VARCHAR(50) NOT NULL,
    department_id BIGINT,
    reporting_manager_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
    FOREIGN KEY (reporting_manager_id) REFERENCES employee(id) ON DELETE SET NULL,
    INDEX idx_emp_code (employee_code),
    INDEX idx_emp_email (email),
    INDEX idx_emp_phone (phone),
    INDEX idx_emp_department (department_id),
    INDEX idx_emp_role (role),
    INDEX idx_emp_active (is_active),
    INDEX idx_emp_manager (reporting_manager_id)
);

-- =============================================
-- 3. EMPLOYEE HISTORY LOG TABLE (for tracking changes)
-- =============================================
CREATE TABLE employee_history_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    INDEX idx_history_employee (employee_id),
    INDEX idx_history_action (action_type),
    INDEX idx_history_date (changed_at)
);

-- =============================================
-- 4. ATTENDANCE TABLE
-- =============================================
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    check_in_time DATETIME,
    check_out_time DATETIME,
    status VARCHAR(50) NOT NULL,
    total_working_hours DOUBLE,
    overtime_hours DOUBLE,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    UNIQUE KEY uk_attendance_employee_date (employee_id, date),
    INDEX idx_attendance_employee (employee_id),
    INDEX idx_attendance_date (date),
    INDEX idx_attendance_status (status)
);

-- =============================================
-- 5. LEAVE TABLE (with workflow)
-- =============================================
CREATE TABLE leave_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    approved_by BIGINT,
    approved_at DATETIME,
    rejected_by BIGINT,
    rejected_at DATETIME,
    rejection_reason TEXT,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES employee(id) ON DELETE SET NULL,
    FOREIGN KEY (rejected_by) REFERENCES employee(id) ON DELETE SET NULL,
    INDEX idx_leave_employee (employee_id),
    INDEX idx_leave_dates (start_date, end_date),
    INDEX idx_leave_status (status),
    INDEX idx_leave_type (leave_type)
);

-- =============================================
-- 6. LEAVE BALANCE TABLE
-- =============================================
CREATE TABLE leave_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    year INT NOT NULL,
    annual_leave_balance DECIMAL(5, 2) DEFAULT 0,
    casual_leave_balance DECIMAL(5, 2) DEFAULT 0,
    sick_leave_balance DECIMAL(5, 2) DEFAULT 0,
    total_leave_taken DECIMAL(5, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    UNIQUE KEY uk_leave_balance_employee_year (employee_id, year),
    INDEX idx_balance_employee (employee_id),
    INDEX idx_balance_year (year)
);

-- =============================================
-- 7. LEAVE TYPE TABLE (Only once!)
-- =============================================
CREATE TABLE leave_type (
    leave_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    annual_limit INT,
    description TEXT
);

-- Insert default leave types
INSERT INTO leave_type (type_name, annual_limit, description) VALUES
('ANNUAL', 20, 'Annual leave / Vacation leave'),
('SICK', 12, 'Sick leave for medical purposes'),
('CASUAL', 10, 'Casual leave for personal matters'),
('UNPAID', 30, 'Unpaid leave'),
('MATERNITY', 90, 'Maternity leave for new mothers'),
('PATERNITY', 15, 'Paternity leave for new fathers'),
('BEREAVEMENT', 10, 'Leave for family bereavement');

-- =============================================
-- 8. PAYROLL TABLE
-- =============================================
CREATE TABLE payroll (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    basic_salary DECIMAL(12, 2) NOT NULL,
    
    -- Earnings
    house_rent_allowance DECIMAL(12, 2),
    dearness_allowance DECIMAL(12, 2),
    travel_allowance DECIMAL(12, 2),
    medical_allowance DECIMAL(12, 2),
    special_allowance DECIMAL(12, 2),
    bonus DECIMAL(12, 2),
    overtime_pay DECIMAL(12, 2),
    total_earnings DECIMAL(12, 2) NOT NULL,
    
    -- Deductions
    provident_fund DECIMAL(12, 2),
    professional_tax DECIMAL(12, 2),
    income_tax DECIMAL(12, 2),
    loan_deduction DECIMAL(12, 2),
    other_deductions DECIMAL(12, 2),
    total_deductions DECIMAL(12, 2) NOT NULL,
    
    -- Net Pay
    net_salary DECIMAL(12, 2) NOT NULL,
    
    -- Attendance Details
    total_working_days INT,
    total_present_days INT,
    total_absent_days INT,
    total_leave_days INT,
    total_overtime_hours DOUBLE,
    
    -- Payment Details
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    payment_date DATE,
    payment_method VARCHAR(50),
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    
    -- Audit Information
    generated_at DATETIME NOT NULL,
    generated_by VARCHAR(100),
    approved_by VARCHAR(100),
    approved_at DATETIME,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    UNIQUE KEY uk_payroll_employee_month (employee_id, year, month),
    INDEX idx_payroll_employee (employee_id),
    INDEX idx_payroll_date (year, month),
    INDEX idx_payroll_status (payment_status)
);

-- =============================================
-- 9. USER TABLE (for authentication - extending employee)
-- =============================================
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    employee_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    last_login DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE SET NULL,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
);

-- =============================================
-- 10. NOTIFICATION TABLE
-- =============================================
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_read (is_read),
    INDEX idx_notification_created (created_at)
);

-- =============================================
-- 11. AUDIT LOG TABLE
-- =============================================
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value JSON,
    new_value JSON,
    performed_by VARCHAR(100),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    INDEX idx_audit_entity (entity_name, entity_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_date (performed_at)
);

-- =============================================
-- INSERT SAMPLE DATA
-- =============================================

-- Insert Departments
INSERT INTO department (name, department_code, description, is_active) VALUES
('Engineering', 'ENG', 'Software Engineering Department', TRUE),
('Human Resources', 'HR', 'Human Resources Department', TRUE),
('Finance', 'FIN', 'Finance and Accounting Department', TRUE),
('Sales', 'SALES', 'Sales and Marketing Department', TRUE),
('Operations', 'OPS', 'Operations Department', TRUE);

-- Insert Employees
INSERT INTO employee (employee_code, first_name, last_name, email, phone, address, position, base_salary, joining_date, role, department_id, is_active) VALUES
('EMP001', 'vikram', 'v', 'vikram.v@mail.com', '+1234567890', '123 Main St, City', 'Software Engineer', 75000.00, '2023-01-15', 'ROLE_EMPLOYEE', 1, TRUE),
('EMP002', 'bala', 'b', 'bala.b@mail.com', '+1987654321', '456 Oak Ave, City', 'Senior Engineer', 95000.00, '2022-06-01', 'ROLE_EMPLOYEE', 1, TRUE),
('EMP003', 'dharun', 'd', 'dharun.d@mail.com', '+1555555555', '789 Pine Rd, City', 'Product Manager', 85000.00, '2023-03-10', 'ROLE_MANAGER', 1, TRUE),
('EMP004', 'guru', 'g', 'guru.g@mail.com', '+1122334455', '321 East St, City', 'HR Manager', 90000.00, '2022-01-10', 'ROLE_ADMIN', 2, TRUE),
('EMP005', 'hari', 's', 'hari.s@mail.com', '+1666777888', '654 Maple St, City', 'Accountant', 70000.00, '2023-02-20', 'ROLE_EMPLOYEE', 3, TRUE),
('EMP006', 'priya', 'p', 'priya.p@mail.com', '+1999888777', '987 Anna St, City', 'Sales Executive', 72000.00, '2023-04-05', 'ROLE_EMPLOYEE', 4, TRUE);

-- Update reporting managers
UPDATE employee SET reporting_manager_id = 3 WHERE id IN (1, 2);
UPDATE employee SET reporting_manager_id = 4 WHERE id IN (3, 5, 6);

-- Insert Leave Balances for current year
INSERT INTO leave_balance (employee_id, year, annual_leave_balance, casual_leave_balance, sick_leave_balance, total_leave_taken) VALUES
(1, 2024, 20, 12, 15, 5),
(2, 2024, 20, 12, 15, 3),
(3, 2024, 20, 12, 15, 2),
(4, 2024, 20, 12, 15, 1),
(5, 2024, 20, 12, 15, 4),
(6, 2024, 20, 12, 15, 2);

-- Insert Sample Leave Applications
INSERT INTO leave_application (employee_id, leave_type, start_date, end_date, total_days, reason, status, created_at) VALUES
(1, 'ANNUAL', DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 3, 'Family vacation', 'PENDING', NOW()),
(2, 'SICK', DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 'Not feeling well', 'PENDING', NOW()),
(5, 'CASUAL', DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY), 1, 'Personal work', 'APPROVED', NOW());

-- Insert Sample Attendance for current date
INSERT INTO attendance (employee_id, date, check_in_time, check_out_time, status, total_working_hours, remarks) VALUES
(1, CURDATE(), CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:30:00'), 'PRESENT', 8.5, 'Regular day'),
(2, CURDATE(), CONCAT(CURDATE(), ' 08:45:00'), CONCAT(CURDATE(), ' 17:15:00'), 'PRESENT', 8.5, 'Early bird'),
(3, CURDATE(), CONCAT(CURDATE(), ' 09:15:00'), CONCAT(CURDATE(), ' 17:45:00'), 'LATE', 8.5, 'Traffic delay'),
(4, CURDATE(), CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 18:00:00'), 'PRESENT', 9.0, NULL),
(5, CURDATE(), CONCAT(CURDATE(), ' 09:30:00'), CONCAT(CURDATE(), ' 17:30:00'), 'LATE', 8.0, 'Missed bus'),
(6, CURDATE(), CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:00:00'), 'HALF_DAY', 8.0, 'Left early for appointment');

-- Insert Users (for authentication)
INSERT INTO user (username, password, email, role, employee_id, is_active) VALUES
('vikram.v', '$2a$10$N.ZuOxQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQ', 'vikram.v@mail.com', 'ROLE_EMPLOYEE', 1, TRUE),
('bala.b', '$2a$10$N.ZuOxQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQ', 'bala.b@mail.com', 'ROLE_EMPLOYEE', 2, TRUE),
('dharun.d', '$2a$10$N.ZuOxQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQ', 'dharun.d@mail.com', 'ROLE_MANAGER', 3, TRUE),
('guru.g', '$2a$10$N.ZuOxQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQzQ', 'guru.g@mail.com', 'ROLE_ADMIN', 4, TRUE);

-- Create stored procedure for monthly payroll processing
DELIMITER //

CREATE PROCEDURE process_monthly_payroll(IN p_year INT, IN p_month INT)
BEGIN
    INSERT INTO payroll (
        employee_id, year, month, basic_salary,
        total_earnings, total_deductions, net_salary,
        generated_at, generated_by
    )
    SELECT 
        e.id, p_year, p_month, e.base_salary,
        e.base_salary as total_earnings,
        (e.base_salary * 0.12) as total_deductions,
        (e.base_salary - (e.base_salary * 0.12)) as net_salary,
        NOW(), 'SYSTEM'
    FROM employee e
    WHERE e.is_active = TRUE
    ON DUPLICATE KEY UPDATE
        generated_at = NOW();
END//

DELIMITER ;

-- Create view for employee attendance summary
CREATE VIEW employee_attendance_summary AS
SELECT 
    e.id as employee_id,
    e.employee_code,
    e.first_name,
    e.last_name,
    e.position,
    COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) as total_present,
    COUNT(CASE WHEN a.status = 'LATE' THEN 1 END) as total_late,
    COUNT(CASE WHEN a.status = 'HALF_DAY' THEN 1 END) as total_half_days,
    COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END) as total_absent,
    ROUND(AVG(a.total_working_hours), 2) as avg_working_hours
FROM employee e
LEFT JOIN attendance a ON e.id = a.employee_id
WHERE a.date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY e.id;

-- Create view for leave summary
CREATE VIEW leave_summary AS
SELECT 
    e.id as employee_id,
    e.employee_code,
    e.first_name,
    e.last_name,
    la.leave_type,
    COUNT(*) as total_applied,
    SUM(CASE WHEN la.status = 'APPROVED' THEN la.total_days ELSE 0 END) as total_approved_days,
    SUM(CASE WHEN la.status = 'PENDING' THEN la.total_days ELSE 0 END) as pending_days
FROM employee e
LEFT JOIN leave_application la ON e.id = la.employee_id
WHERE la.start_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
GROUP BY e.id, la.leave_type;

COMMIT;

-- Display confirmation
SELECT 'HRMS Database Setup Complete!' as Status;
SELECT COUNT(*) as Total_Departments FROM department;
SELECT COUNT(*) as Total_Employees FROM employee;
SELECT COUNT(*) as Total_Users FROM user;