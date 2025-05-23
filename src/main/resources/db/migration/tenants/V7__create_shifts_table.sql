-- Create shifts table (if not already present)
CREATE TABLE IF NOT EXISTS shifts (
                                      id BIGSERIAL PRIMARY KEY,
                                      shift_name VARCHAR(100) NOT NULL UNIQUE,
    shift_label VARCHAR(100),
    color VARCHAR(30),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    calendar_date DATE,
    weekly_off BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_shifts_name ON shifts(shift_name);

-- Create shift_rotations table
CREATE TABLE IF NOT EXISTS shift_rotations (
                                               id BIGSERIAL PRIMARY KEY,
                                               rotation_name VARCHAR(100) NOT NULL UNIQUE,
    weeks INT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
    );

-- Create shift_rotation_days table (multi-week mapping)
CREATE TABLE IF NOT EXISTS shift_rotation_days (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   shift_rotation_id BIGINT NOT NULL REFERENCES shift_rotations(id) ON DELETE CASCADE,
    week INT NOT NULL, -- 1-based week number
    weekday VARCHAR(10) NOT NULL, -- e.g. "Sun", "Mon", etc.
    shift_id BIGINT NOT NULL REFERENCES shifts(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_shift_rotation_days_rotation_id ON shift_rotation_days(shift_rotation_id);
CREATE INDEX IF NOT EXISTS idx_shift_rotation_days_week ON shift_rotation_days(week);
CREATE INDEX IF NOT EXISTS idx_shift_rotation_days_shift_id ON shift_rotation_days(shift_id);

-- Prevent duplicate week+weekday for the same rotation
ALTER TABLE shift_rotation_days
    ADD CONSTRAINT uq_rotation_week_weekday UNIQUE (shift_rotation_id, week, weekday);
