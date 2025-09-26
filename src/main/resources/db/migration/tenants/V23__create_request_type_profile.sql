-- V23__create_request_type_profile.sql

-- Creates the main table to store request type profiles.
CREATE TABLE request_type_profile (
                                      id BIGSERIAL PRIMARY KEY,
                                      profile_name VARCHAR(255) NOT NULL UNIQUE
);

-- Creates the join table for the many-to-many relationship
CREATE TABLE request_type_profile_request_types (
                                                    request_type_profile_id BIGINT NOT NULL,
                                                    request_type_id BIGINT NOT NULL,
                                                    PRIMARY KEY (request_type_profile_id, request_type_id),
                                                    CONSTRAINT fk_request_type_profile
                                                        FOREIGN KEY (request_type_profile_id) REFERENCES request_type_profile(id) ON DELETE CASCADE,
                                                    CONSTRAINT fk_request_type
                                                        FOREIGN KEY (request_type_id) REFERENCES request_types(id) ON DELETE CASCADE
);
