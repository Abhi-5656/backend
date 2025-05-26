-- V8__create_holidays_table.sql

-- Create holidays table
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

-- Create holiday_profiles table
CREATE TABLE IF NOT EXISTS holiday_profiles (
                                                id BIGSERIAL PRIMARY KEY,
                                                profile_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_holiday_profiles_name ON holiday_profiles(profile_name);

-- Create join table for holiday_profiles <-> holidays
CREATE TABLE IF NOT EXISTS holiday_profile_holidays (
                                                        profile_id BIGINT NOT NULL REFERENCES holiday_profiles(id) ON DELETE CASCADE,
    holiday_id BIGINT NOT NULL REFERENCES holidays(id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, holiday_id)
    );

CREATE INDEX IF NOT EXISTS idx_profile_id ON holiday_profile_holidays(profile_id);
CREATE INDEX IF NOT EXISTS idx_holiday_id ON holiday_profile_holidays(holiday_id);
