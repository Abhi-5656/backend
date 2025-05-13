-- ✅ Enable UUID Extension (if not already enabled) - pgcrypto is generally for UUID generation, not strictly needed for this schema but often included.
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ✅ Create Roles Table inside Tenant Schema
CREATE TABLE IF NOT EXISTS roles (
                                     id SERIAL PRIMARY KEY,
                                     role_name VARCHAR(50) UNIQUE NOT NULL
    );

-- ✅ Insert Default Roles (Including HR)
INSERT INTO roles (role_name) VALUES
                                  ('ADMIN'),
                                  ('MANAGER'),
                                  ('EMPLOYEE'),
                                  ('HR')
    ON CONFLICT (role_name) DO NOTHING;  -- Prevent duplicate inserts

-- ✅ Create Employees Table inside Tenant Schema
-- This table now includes columns from Employee direct fields, PersonalInfo, and OrganizationalInfo (via EmploymentDetails)
CREATE TABLE IF NOT EXISTS employees (
    -- Core Employee Fields
                                         id SERIAL PRIMARY KEY,
                                         employee_id VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL, -- Work Email, used for login
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL, -- Primary/Work Mobile Number
    role_id INT NOT NULL,
    tenant_id VARCHAR(50), -- Stores the tenant identifier string
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                             -- Foreign Key for Role (defined within this script)
                             CONSTRAINT fk_employee_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT, -- Changed to RESTRICT from CASCADE for roles

-- Foreign Keys for self-referencing manager fields (can be defined here)
    reporting_manager_id BIGINT,
    hr_manager_id BIGINT,
    CONSTRAINT fk_employee_reporting_manager FOREIGN KEY (reporting_manager_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT fk_employee_hr_manager FOREIGN KEY (hr_manager_id) REFERENCES employees(id) ON DELETE SET NULL,

    -- Foreign Keys for organizational structure (to be added in a later migration after V4)
    work_location_id BIGINT,
    business_unit_id BIGINT,
    job_title_id BIGINT, -- Designation

-- Columns from PersonalInfo (Embedded)
    first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255), -- Derived, but good to have the column
    display_name VARCHAR(255), -- Derived or user-defined
    gender VARCHAR(50),
    date_of_birth DATE,
    blood_group VARCHAR(50),
    marital_status VARCHAR(50),
    pan_number VARCHAR(10) UNIQUE,
    aadhaar_number VARCHAR(12) UNIQUE,
    nationality VARCHAR(100),
    personal_email VARCHAR(255) UNIQUE,
    alternate_mobile VARCHAR(20),
    emergency_contact_name VARCHAR(255),
    emergency_contact_number VARCHAR(20),
    emergency_contact_relationship VARCHAR(50),
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
    permanent_pincode VARCHAR(10),

    -- Columns from OrganizationalInfo -> EmploymentDetails (prefixed with 'org_info_')
    -- These prefixes match the @AttributeOverrides in OrganizationalInfo.java (artifact organizational_info_entity_v3)
    org_info_date_of_joining DATE, -- Made nullable, will be NOT NULL via Java validation if needed for specific cases
    org_info_employment_type VARCHAR(50),
    org_info_employment_status VARCHAR(50),
    org_info_confirmation_date DATE,
    org_info_probation_period_months INT,
    org_info_notice_period_days INT,
    org_info_work_mode VARCHAR(50),
    org_info_department_name VARCHAR(255),
    org_info_job_grade_band VARCHAR(100),
    org_info_cost_center VARCHAR(100),
    org_info_role_description VARCHAR(255),
    org_info_role_effective_date DATE
    );

-- ✅ Create Index for Faster Lookups on `tenant_id`
CREATE INDEX IF NOT EXISTS idx_employees_tenant_id ON employees(tenant_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_employee_id ON employees(employee_id);


-- ✅ Create Trigger Function to Auto-Update `updated_at`
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ✅ Create Trigger on Employees Table
-- Drop the trigger first if it exists, to ensure it can be recreated if changed
DROP TRIGGER IF EXISTS set_timestamp ON employees;
CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
