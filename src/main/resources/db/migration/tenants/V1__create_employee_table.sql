-- ✅ Enable UUID Extension (if not already enabled)
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
CREATE TABLE IF NOT EXISTS employees (
                                         id SERIAL PRIMARY KEY,  -- ✅ Keep SERIAL for Primary Key
                                         first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,  -- ✅ Store concatenated full name
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,  -- ✅ Now Unique
    employee_id VARCHAR(50) UNIQUE NOT NULL,  -- ✅ Unique Employee ID
    password VARCHAR(255) NOT NULL,  -- ✅ Store Encrypted Password
    tenant_id VARCHAR(50),  -- ✅ Changed from UUID to STRING for Multi-Tenancy
    role_id INT NOT NULL,  -- ✅ Reference Role Table
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- ✅ Default on Insert
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- ✅ Initial Default
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

-- ✅ Create Index for Faster Lookups on `tenant_id`
CREATE INDEX idx_employees_tenant_id ON employees(tenant_id);

-- ✅ Create Trigger Function to Auto-Update `updated_at`
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ✅ Create Trigger on Employees Table
CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
