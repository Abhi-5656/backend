-- V12__create_shift_rotation_assignment_table.sql

-- 1. Shift Rotation Assignment Table
CREATE TABLE employee_shift_rotation_assignments (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     employee_id VARCHAR(50) NOT NULL,
                                                     shift_rotation_id BIGINT NOT NULL,
                                                     effective_date DATE NOT NULL,
                                                     expiration_date DATE,
                                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                     updated_at TIMESTAMP,
                                                     CONSTRAINT fk_shift_rotation
                                                         FOREIGN KEY (shift_rotation_id)
                                                             REFERENCES shift_rotations (id)
                                                             ON DELETE CASCADE
);

-- 2. Employee Shift Table (Derived Daily Roster)
CREATE TABLE employee_shifts (
                                 id BIGSERIAL PRIMARY KEY,
                                 employee_id VARCHAR(50) NOT NULL,
                                 shift_id BIGINT,                    -- Can be NULL for week off/holiday
                                 calendar_date DATE NOT NULL,
                                 is_week_off BOOLEAN NOT NULL DEFAULT FALSE,
                                 is_holiday BOOLEAN NOT NULL DEFAULT FALSE,
                                 weekday VARCHAR(10) NOT NULL,       -- 'MONDAY', 'TUESDAY', etc.
                                 deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                 assigned_by VARCHAR(50),
                                 CONSTRAINT fk_shift
                                     FOREIGN KEY (shift_id)
                                         REFERENCES shifts (id)
                                         ON DELETE CASCADE
);
