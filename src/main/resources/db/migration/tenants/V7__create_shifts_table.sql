CREATE TABLE IF NOT EXISTS shifts (
                                      id BIGSERIAL PRIMARY KEY,
                                      shift_name VARCHAR(100) NOT NULL UNIQUE,
    shift_label VARCHAR(100),
    color VARCHAR(30),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    calendar_date DATE,           -- NEW FIELD
    weekly_off BOOLEAN DEFAULT FALSE,  -- NEW FIELD
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

-- Many-to-many: shift_rotation_shifts
CREATE TABLE IF NOT EXISTS shift_rotation_shifts (
                                                     shift_rotation_id BIGINT NOT NULL REFERENCES shift_rotations(id) ON DELETE CASCADE,
    shift_id BIGINT NOT NULL REFERENCES shifts(id) ON DELETE CASCADE,
    PRIMARY KEY (shift_rotation_id, shift_id)
    );

-- Sequence array for rotation: shift_rotation_sequence
CREATE TABLE IF NOT EXISTS shift_rotation_sequence (
                                                       shift_rotation_id BIGINT NOT NULL REFERENCES shift_rotations(id) ON DELETE CASCADE,
    sequence INT,
    PRIMARY KEY (shift_rotation_id, sequence)
    );