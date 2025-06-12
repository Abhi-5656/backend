-- V13__create_devices_table.sql

CREATE TABLE devices (
                         device_id           VARCHAR(64)  PRIMARY KEY,
                         device_name         VARCHAR(128) NOT NULL,
                         device_type         VARCHAR(32)  NOT NULL,
                         serial_number       VARCHAR(64),
                         os_version          VARCHAR(64),
                         firmware_version    VARCHAR(64),
                         location_id         VARCHAR(64),
                         assigned_to         VARCHAR(64),
                         status              VARCHAR(32)  NOT NULL,
                         geo_fence_id        VARCHAR(64),
                         ip_address          VARCHAR(45),
                         remarks             VARCHAR(255),
                         device_photo_url    VARCHAR(255),
                         registered_on       TIMESTAMPTZ  NOT NULL,
                         last_heartbeat      TIMESTAMPTZ,
                         last_punch_time     TIMESTAMPTZ,
                         deleted             BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Table to hold allowed employee IDs per device (one-to-many, as per @ElementCollection in JPA)
CREATE TABLE device_allowed_employee_ids (
                                             device_id   VARCHAR(64) NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
                                             employee_id VARCHAR(64) NOT NULL,
                                             PRIMARY KEY (device_id, employee_id)
);

-- Indexes for performance
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_devices_location_id ON devices(location_id);
CREATE INDEX idx_devices_geo_fence_id ON devices(geo_fence_id);
