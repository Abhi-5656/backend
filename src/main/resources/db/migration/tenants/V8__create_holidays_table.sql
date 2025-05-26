-- V8__create_holidays_table.sql

CREATE TABLE IF NOT EXISTS holidays (
                                        id BIGSERIAL PRIMARY KEY,
                                        holiday_name VARCHAR(100) NOT NULL,
    holiday_type VARCHAR(50) NOT NULL,  -- Use ENUM values: NATIONAL, RELIGIOUS, REGIONAL
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_holidays_name ON holidays(holiday_name);
CREATE INDEX IF NOT EXISTS idx_holidays_type ON holidays(holiday_type);
