-- V17__create_employee_profile_registrations_table.sql
-- ======================================================
-- Table: employee_profile_registrations
-- Tracks if an employee has registered with a profile image.
-- ======================================================

CREATE TABLE IF NOT EXISTS employee_profile_registrations (
                                                              id BIGSERIAL PRIMARY KEY,
                                                              employee_id VARCHAR(64) NOT NULL UNIQUE,

    -- Use BYTEA for PostgreSQL for storing raw binary data.
    -- For other databases like MySQL or H2, you might use BLOB.
    employee_image_data BYTEA,

    has_registered_with_image BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
    );

-- Index for quickly looking up a registration by employee_id
CREATE INDEX IF NOT EXISTS idx_epr_employee_id ON employee_profile_registrations(employee_id);