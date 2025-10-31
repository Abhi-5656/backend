-- V25__create_employee_location_table.sql
-- Enables PostGIS extension if not already enabled in the schema
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create a table to store employee location history
CREATE TABLE employee_locations (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL,
    -- Store location as a Geography point (Longitude, Latitude) with SRID 4326
    location GEOGRAPHY(POINT, 4326) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    punch_type VARCHAR(20) NOT NULL,

    CONSTRAINT fk_employee_location
        FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
        ON DELETE CASCADE
);

-- Create a spatial index for fast geographic queries (e.g., "find employees within 500m")
CREATE INDEX idx_employee_locations_location ON employee_locations USING GIST (location);

-- Create an index for querying by employee and time
CREATE INDEX idx_employee_locations_employee_timestamp ON employee_locations (employee_id, timestamp DESC);