-- V1__create_employee_table.sql
-- Schema for initial employee setup, including minimal admin creation.
-- Fields not provided during admin creation are nullable in the DB.
-- Application-level validation groups (OnAdminCreation, OnEmployeeProfile)
-- will enforce stricter rules for full employee profiles.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Roles Table (Essential for Employee)
CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     role_name VARCHAR(50) UNIQUE NOT NULL
    );

INSERT INTO roles (role_name) VALUES
                                  ('ADMIN'), ('MANAGER'), ('EMPLOYEE'), ('HR')
    ON CONFLICT (role_name) DO NOTHING;

-- Employee Personal Info Table
-- Only firstName and lastName are NOT NULL here, reflecting minimal admin creation needs.
-- Other fields are nullable and will be validated by OnEmployeeProfile group in Java.
CREATE TABLE IF NOT EXISTS employee_personal_info (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      first_name VARCHAR(255) NOT NULL, -- Required for admin
    middle_name VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,  -- Required for admin
    full_name VARCHAR(765),
    display_name VARCHAR(510),
    gender VARCHAR(50) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')), -- Nullable
    date_of_birth DATE,                                              -- Nullable
    blood_group VARCHAR(50) CHECK (blood_group IN ('A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE', 'UNKNOWN')), -- Nullable
    marital_status VARCHAR(50) CHECK (marital_status IN ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED', 'SEPARATED')), -- Nullable
    pan_number VARCHAR(10) UNIQUE,                                   -- Nullable (but unique if present)
    aadhaar_number VARCHAR(12) UNIQUE,                               -- Nullable (but unique if present)
    nationality VARCHAR(100),                                        -- Nullable
    personal_email VARCHAR(255) UNIQUE,                              -- Nullable
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

-- Employee Employment Details Table
-- Core details are NOT NULL as they define basic employment terms for admin.
CREATE TABLE IF NOT EXISTS employee_employment_details (
                                                           id BIGSERIAL PRIMARY KEY,
                                                           date_of_joining DATE NOT NULL,
                                                           employment_type VARCHAR(50) NOT NULL CHECK (employment_type IN ('PERMANENT', 'CONTRACT', 'INTERN', 'TEMPORARY', 'PART_TIME', 'FULL_TIME')),
    employment_status VARCHAR(50) NOT NULL CHECK (employment_status IN ('ACTIVE', 'PROBATION', 'NOTICE_PERIOD', 'TERMINATED', 'RESIGNED', 'ON_LEAVE', 'SUSPENDED', 'ABSCONDING', 'DECEASED')),
    confirmation_date DATE, -- Nullable
    probation_period_months INT CHECK (probation_period_months >= 0), -- Nullable
    notice_period_days INT NOT NULL CHECK (notice_period_days >= 0),
    work_mode VARCHAR(50) NOT NULL CHECK (work_mode IN ('WORK_FROM_OFFICE', 'REMOTE', 'HYBRID'))
    );

-- Employee Job Context Details Table
-- Core details are NOT NULL for basic admin organizational context.
CREATE TABLE IF NOT EXISTS employee_job_context_details (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            department_name VARCHAR(255) NOT NULL,
    job_grade_band VARCHAR(100) NOT NULL,
    cost_center VARCHAR(100) NOT NULL,
    organizational_role_description TEXT -- Nullable
    );

-- Employee Organizational Info Table
-- Links to details tables are NOT NULL as an OrgInfo record implies these details exist.
-- The effective date is also crucial for the assignment.
CREATE TABLE IF NOT EXISTS employee_organizational_info (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            employment_details_id BIGINT UNIQUE NOT NULL, -- FK, UNIQUE for OneToOne, NOT NULL as it's essential part
                                                            job_context_details_id BIGINT UNIQUE NOT NULL,  -- FK, UNIQUE for OneToOne, NOT NULL as it's essential part
                                                            org_assignment_effective_date DATE NOT NULL,

                                                            CONSTRAINT fk_orginfo_employment_details FOREIGN KEY (employment_details_id) REFERENCES employee_employment_details(id) ON DELETE RESTRICT, -- Consider ON DELETE CASCADE if appropriate
    CONSTRAINT fk_orginfo_job_context_details FOREIGN KEY (job_context_details_id) REFERENCES employee_job_context_details(id) ON DELETE RESTRICT  -- Consider ON DELETE CASCADE
    );

-- Employees Table
CREATE TABLE IF NOT EXISTS employees (
                                         id BIGSERIAL PRIMARY KEY,
                                         employee_id VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    role_id BIGINT NOT NULL,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                             personal_info_id BIGINT UNIQUE NOT NULL,     -- FK, NOT NULL as an Employee must have personal info (even if minimal for admin)
                             organizational_info_id BIGINT UNIQUE NOT NULL, -- FK, NOT NULL as an Employee must have org info

                             reporting_manager_id BIGINT,
                             hr_manager_id BIGINT,
                             work_location_id BIGINT,
                             business_unit_id BIGINT,
                             job_title_id BIGINT,

                             CONSTRAINT fk_employee_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    CONSTRAINT fk_employee_personal_info FOREIGN KEY (personal_info_id) REFERENCES employee_personal_info(id) ON DELETE RESTRICT, -- Consider CASCADE
    CONSTRAINT fk_employee_organizational_info FOREIGN KEY (organizational_info_id) REFERENCES employee_organizational_info(id) ON DELETE RESTRICT, -- Consider CASCADE
    CONSTRAINT fk_employee_reporting_manager FOREIGN KEY (reporting_manager_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT fk_employee_hr_manager FOREIGN KEY (hr_manager_id) REFERENCES employees(id) ON DELETE SET NULL
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_employees_tenant_id ON employees(tenant_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_employee_id ON employees(employee_id);
CREATE INDEX IF NOT EXISTS idx_employee_personal_info_pan ON employee_personal_info(pan_number);
CREATE INDEX IF NOT EXISTS idx_employee_personal_info_aadhaar ON employee_personal_info(aadhaar_number);

-- Trigger Function for Employees.updated_at
CREATE OR REPLACE FUNCTION update_employee_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger on Employees Table
DROP TRIGGER IF EXISTS set_employee_timestamp ON employees;
CREATE TRIGGER set_employee_timestamp
    BEFORE UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION update_employee_updated_at_column();