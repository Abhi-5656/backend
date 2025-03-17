-- ✅ Create Roles Table inside Tenant Schema
CREATE TABLE IF NOT EXISTS roles (
                                     id SERIAL PRIMARY KEY,
                                     role_name VARCHAR(50) UNIQUE NOT NULL
    );

-- ✅ Insert Default Roles
INSERT INTO roles (role_name) VALUES
                                  ('ADMIN'),
                                  ('MANAGER'),
                                  ('EMPLOYEE')
    ON CONFLICT (role_name) DO NOTHING;  -- Prevent duplicate inserts

-- ✅ Enable UUID Extension (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ✅ Create Employees Table inside Tenant Schema
CREATE TABLE IF NOT EXISTS employees (
                                         id SERIAL PRIMARY KEY,  -- ✅ Keep SERIAL for Primary Key
                                         first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,  -- ✅ Store concatenated full name
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    employee_id VARCHAR(50) UNIQUE NOT NULL,  -- ✅ Unique Employee ID
    password VARCHAR(255) NOT NULL,  -- ✅ Store Encrypted Password
    tenant_id UUID NOT NULL,  -- ✅ Tenant ID as UUID for Multi-Tenancy
    role_id INT NOT NULL,  -- ✅ Reference Role Table
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

