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
