-- V19__create_leave_profile.sql

-- Creates the main table to store leave profiles.
CREATE TABLE leave_profile (
                               id BIGSERIAL PRIMARY KEY,
                               profile_name VARCHAR(255) NOT NULL UNIQUE
);

-- Creates the join table to manage the many-to-many relationship
-- between a leave profile and its associated leave policies.
CREATE TABLE leave_profile_policies (
                                        leave_profile_id BIGINT NOT NULL,
                                        leave_policy_id BIGINT NOT NULL,
                                        PRIMARY KEY (leave_profile_id, leave_policy_id),
                                        CONSTRAINT fk_leave_profile
                                            FOREIGN KEY (leave_profile_id) REFERENCES leave_profile(id) ON DELETE CASCADE,
                                        CONSTRAINT fk_leave_policy
                                            FOREIGN KEY (leave_policy_id) REFERENCES leave_policy(id) ON DELETE CASCADE
);