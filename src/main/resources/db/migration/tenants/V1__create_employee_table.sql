-- V1__employee_roles_many_to_many.sql
-- ===================================================================
-- Schema: Roles, Employees (+ related info tables), Permissions,
--         Employee<->Roles (M:N), Role<->Permissions (M:N)
--         Seed data for roles, permissions, and default mappings
-- Target: PostgreSQL (transactional)
-- ===================================================================

-- ===============================
-- ROLES TABLE
CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     role_name VARCHAR(50) UNIQUE NOT NULL
    );

-- Seed Roles
INSERT INTO roles (role_name) VALUES
                                  ('Admin'),
                                  ('Manager'),
                                  ('Employee'),
                                  ('HR')
    ON CONFLICT (role_name) DO NOTHING;

-- ===============================
-- EMPLOYEE_PERSONAL_INFO TABLE
CREATE TABLE IF NOT EXISTS employee_personal_info (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(765),
    display_name VARCHAR(510),
    gender VARCHAR(50) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    date_of_birth DATE,
    blood_group VARCHAR(50) CHECK (blood_group IN ('A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE', 'UNKNOWN')),
    marital_status VARCHAR(50) CHECK (marital_status IN ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED', 'SEPARATED')),
    pan_number VARCHAR(10) UNIQUE,
    aadhaar_number VARCHAR(12) UNIQUE,
    nationality VARCHAR(100),
    personal_email VARCHAR(255) UNIQUE,
    alternate_mobile VARCHAR(20),
    emergency_contact_name VARCHAR(255),
    emergency_contact_number VARCHAR(20),
    emergency_contact_relationship VARCHAR(50) CHECK (emergency_contact_relationship IN ('FATHER', 'MOTHER', 'SPOUSE', 'SIBLING', 'CHILD', 'FRIEND', 'OTHER')),
    current_address_line_1 VARCHAR(255),
    current_address_line_2 VARCHAR(255),
    current_state VARCHAR(100),
    current_city VARCHAR(100),
    current_pincode VARCHAR(10),
    is_permanent_same_as_current BOOLEAN DEFAULT FALSE,
    permanent_address_line_1 VARCHAR(255),
    permanent_address_line_2 VARCHAR(255),
    permanent_state VARCHAR(100),
    permanent_city VARCHAR(100),
    permanent_pincode VARCHAR(10)
    );

-- Indexes (personal info)
CREATE INDEX IF NOT EXISTS idx_employee_personal_info_pan ON employee_personal_info(pan_number);
CREATE INDEX IF NOT EXISTS idx_employee_personal_info_aadhaar ON employee_personal_info(aadhaar_number);

-- ===============================
-- EMPLOYEE_EMPLOYMENT_DETAILS TABLE
CREATE TABLE IF NOT EXISTS employee_employment_details (
                                                           id BIGSERIAL PRIMARY KEY,
                                                           date_of_joining DATE NOT NULL,
                                                           employment_type VARCHAR(50) NOT NULL CHECK (employment_type IN ('PERMANENT', 'CONTRACT', 'INTERN', 'TEMPORARY', 'PART_TIME', 'FULL_TIME')),
    employment_status VARCHAR(50) NOT NULL CHECK (employment_status IN ('ACTIVE', 'PROBATION', 'NOTICE_PERIOD', 'TERMINATED', 'RESIGNED', 'ON_LEAVE', 'SUSPENDED', 'ABSCONDING', 'DECEASED')),
    confirmation_date DATE,
    probation_period_months INT CHECK (probation_period_months >= 0),
    notice_period_days INT NOT NULL CHECK (notice_period_days >= 0),
    work_mode VARCHAR(50) NOT NULL CHECK (work_mode IN ('WORK_FROM_OFFICE', 'REMOTE', 'HYBRID'))
    );

-- ===============================
-- EMPLOYEE_JOB_CONTEXT_DETAILS TABLE
CREATE TABLE IF NOT EXISTS employee_job_context_details (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            department_name VARCHAR(255) NOT NULL,
    job_grade_band VARCHAR(100) NOT NULL,
    cost_center VARCHAR(100) NOT NULL,
    organizational_role_description TEXT
    );

-- ===============================
-- EMPLOYEE_ORGANIZATIONAL_INFO TABLE
CREATE TABLE IF NOT EXISTS employee_organizational_info (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            employment_details_id BIGINT UNIQUE NOT NULL,
                                                            job_context_details_id BIGINT UNIQUE NOT NULL,
                                                            org_assignment_effective_date DATE NOT NULL,
                                                            CONSTRAINT fk_orginfo_employment_details
                                                            FOREIGN KEY (employment_details_id)
    REFERENCES employee_employment_details(id) ON DELETE RESTRICT,
    CONSTRAINT fk_orginfo_job_context_details
    FOREIGN KEY (job_context_details_id)
    REFERENCES employee_job_context_details(id) ON DELETE RESTRICT
    );

-- ===============================
-- EMPLOYEES TABLE (NO role_id FIELD)
CREATE TABLE IF NOT EXISTS employees (
                                         id BIGSERIAL PRIMARY KEY,
                                         employee_id VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             personal_info_id BIGINT UNIQUE NOT NULL,
                             organizational_info_id BIGINT UNIQUE NOT NULL,
                             reporting_manager_id BIGINT,
                             hr_manager_id BIGINT,
                             work_location_id BIGINT,
                             business_unit_id BIGINT,
                             job_title_id BIGINT,
                             CONSTRAINT fk_employee_personal_info
                             FOREIGN KEY (personal_info_id) REFERENCES employee_personal_info(id) ON DELETE RESTRICT,
    CONSTRAINT fk_employee_organizational_info
    FOREIGN KEY (organizational_info_id) REFERENCES employee_organizational_info(id) ON DELETE RESTRICT,
    CONSTRAINT fk_employee_reporting_manager
    FOREIGN KEY (reporting_manager_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT fk_employee_hr_manager
    FOREIGN KEY (hr_manager_id) REFERENCES employees(id) ON DELETE SET NULL
    );

-- Indexes (employees)
CREATE INDEX IF NOT EXISTS idx_employees_tenant_id ON employees(tenant_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_employee_id ON employees(employee_id);

-- UPDATED_AT trigger
CREATE OR REPLACE FUNCTION update_employee_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_employee_timestamp ON employees;
CREATE TRIGGER set_employee_timestamp
    BEFORE UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION update_employee_updated_at_column();

-- ===============================
-- EMPLOYEE ROLES JOIN TABLE (UNIDIRECTIONAL MANY-TO-MANY)
CREATE TABLE IF NOT EXISTS employee_roles (
                                              employee_id BIGINT NOT NULL,
                                              role_id BIGINT NOT NULL,
                                              PRIMARY KEY (employee_id, role_id),
    CONSTRAINT fk_employee_roles_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_roles_role
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_employee_roles_employee_id ON employee_roles(employee_id);
CREATE INDEX IF NOT EXISTS idx_employee_roles_role_id ON employee_roles(role_id);

-- ===============================
-- PERMISSIONS TABLE
CREATE TABLE IF NOT EXISTS permissions (
                                           id BIGSERIAL PRIMARY KEY,
                                           name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    module_name VARCHAR(100) NOT NULL
    );

-- ===============================
-- ROLE_PERMISSIONS JOIN TABLE
CREATE TABLE IF NOT EXISTS role_permissions (
                                                role_id BIGINT NOT NULL,
                                                permission_id BIGINT NOT NULL,
                                                PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);

-- ===============================
-- SEED PERMISSIONS (from your list)
INSERT INTO permissions (name, description, module_name) VALUES
-- Employee (CRUD)
('employee:create', 'Allows a user to create new employees.', 'Employee'),
('employee:read', 'Allows a user to view employee details.', 'Employee'),
('employee:readAll', 'Allows a user to view details of all employees.', 'Employee'),
('employee:update', 'Allows a user to update employee details.', 'Employee'),
('employee:delete', 'Allows a user to delete or terminate employees.', 'Employee'),

-- WFM Setup
('wfm:setup:manage', 'Allows User to access all WFM setup modules.', 'WFM Setup'),

-- Timesheet
('timesheet:read', 'Allows a user to view a single timesheet.', 'Timesheet'),
('timesheet:readAll', 'Allows a user to view all timesheets.', 'Timesheet'),
('timesheet:delete', 'Allows a user to delete a timesheet.', 'Timesheet'),
('timesheet:read:own', 'Allows a user to view their own timesheets.', 'Timesheet'),
('timesheet:create', 'Allows a user to create a new timesheet.', 'Timesheet'),
('timesheet:update', 'Allows a user to update an existing timesheet.', 'Timesheet'),

-- WFM Employee Assignment
('wfm:employee:holiday-profile-assignment:assign', 'Allows a user to assign holiday profiles to employees.', 'WFM Employee Assignment'),
('wfm:employee:holiday-profile-assignment:read', 'Allows a user to view employee holiday profile assignments.', 'WFM Employee Assignment'),
('wfm:employee:holiday-profile-assignment:delete', 'Allows a user to deactivate employee holiday profile assignments.', 'WFM Employee Assignment'),

('wfm:employee:pay-policy-assignment:assign', 'Allows a user to assign pay policies to employees.', 'WFM Employee Assignment'),
('wfm:employee:pay-policy-assignment:read', 'Allows a user to view employee pay policy assignments.', 'WFM Employee Assignment'),

('wfm:employee:shift-rotation-assignment:assign', 'Allows a user to assign shift rotations to employees.', 'WFM Employee Assignment'),
('wfm:employee:shift-rotation-assignment:update', 'Allows a user to update employee shift rotation assignments.', 'WFM Employee Assignment'),
('wfm:employee:shift-rotation-assignment:read', 'Allows a user to view employee shift rotation assignments.', 'WFM Employee Assignment'),
('wfm:employee:shift-rotation-assignment:delete', 'Allows a user to delete employee shift rotation assignments.', 'WFM Employee Assignment')
    ON CONFLICT (name) DO NOTHING;

-- ===============================
-- DEFAULT ROLE-PERMISSION MAPPINGS
-- Helper CTEs for IDs
WITH role_ids AS (
    SELECT
        (SELECT id FROM roles WHERE role_name = 'Admin')   AS admin_id,
        (SELECT id FROM roles WHERE role_name = 'HR')      AS hr_id,
        (SELECT id FROM roles WHERE role_name = 'Manager') AS mgr_id,
        (SELECT id FROM roles WHERE role_name = 'Employee')AS emp_id
),
     perm AS (
         SELECT name, id FROM permissions
     )
-- Admin: all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.admin_id, p.id FROM role_ids r CROSS JOIN perm p
    ON CONFLICT DO NOTHING;

-- HR: Most employee & timesheet management + assignments (no setup manage)
WITH role_ids AS (
    SELECT (SELECT id FROM roles WHERE role_name = 'HR') AS role_id
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.id
FROM role_ids r
         JOIN permissions p ON p.name IN (
    -- Employee CRUD (including readAll)
                                          'employee:create','employee:read','employee:readAll','employee:update','employee:delete',
    -- Timesheet
                                          'timesheet:read','timesheet:readAll','timesheet:update','timesheet:create',
    -- Assignments
                                          'wfm:employee:holiday-profile-assignment:assign',
                                          'wfm:employee:holiday-profile-assignment:read',
                                          'wfm:employee:holiday-profile-assignment:delete',
                                          'wfm:employee:pay-policy-assignment:assign',
                                          'wfm:employee:pay-policy-assignment:read',
                                          'wfm:employee:shift-rotation-assignment:assign',
                                          'wfm:employee:shift-rotation-assignment:update',
                                          'wfm:employee:shift-rotation-assignment:read'
    )
    ON CONFLICT DO NOTHING;

-- Manager: read employees (scoped via ABAC in app), manage shift rotations, view timesheets
WITH role_ids AS (
    SELECT (SELECT id FROM roles WHERE role_name = 'Manager') AS role_id
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.id
FROM role_ids r
         JOIN permissions p ON p.name IN (
                                          'employee:read',
    -- Timesheet viewing for team (scope enforced at app/query layer)
                                          'timesheet:read','timesheet:readAll',
    -- Shift-rotation management
                                          'wfm:employee:shift-rotation-assignment:assign',
                                          'wfm:employee:shift-rotation-assignment:update',
                                          'wfm:employee:shift-rotation-assignment:read'
    )
    ON CONFLICT DO NOTHING;

-- Employee: only own timesheet read
WITH role_ids AS (
    SELECT (SELECT id FROM roles WHERE role_name = 'Employee') AS role_id
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.id
FROM role_ids r
         JOIN permissions p ON p.name IN (
    'timesheet:read:own'
    )
    ON CONFLICT DO NOTHING;

-- ===============================
-- DONE
-- ===================================================================
