-- src/main/resources/db/migration/tenants/V18__request_type_schema.sql
CREATE TABLE IF NOT EXISTS approval_configs (
                                                id BIGINT PRIMARY KEY,
                                                enabled BOOLEAN NOT NULL,
                                                mode VARCHAR(255),
    chain_steps TEXT[],
    notify TEXT[],
    escalate INTEGER,
    auto_days INTEGER
    );

CREATE TABLE IF NOT EXISTS clubbing_configs (
                                                id BIGINT PRIMARY KEY,
                                                enabled BOOLEAN NOT NULL,
                                                "with" TEXT[],
                                                weekends BOOLEAN,
                                                hols BOOLEAN,
                                                max INTEGER
);

CREATE TABLE IF NOT EXISTS validation_configs (
                                                  id BIGINT PRIMARY KEY,
                                                  enabled BOOLEAN NOT NULL,
                                                  sandwich BOOLEAN,
                                                  holiday_count BOOLEAN,
                                                  overlap BOOLEAN,
                                                  probation BOOLEAN,
                                                  attachment_mandatory BOOLEAN,
                                                  attachment_days INTEGER
);

CREATE TABLE IF NOT EXISTS notification_configs (
                                                    id BIGINT PRIMARY KEY,
                                                    enabled BOOLEAN NOT NULL,
                                                    channels TEXT[],
                                                    emp_submit BOOLEAN,
                                                    emp_approve BOOLEAN,
                                                    emp_reject BOOLEAN,
                                                    emp_cancel BOOLEAN,
                                                    approver_submit BOOLEAN,
                                                    approver_escalate BOOLEAN,
                                                    approver_cancel BOOLEAN,
                                                    approver_delete BOOLEAN,
                                                    listener_submit BOOLEAN,
                                                    listener_approve BOOLEAN,
                                                    listener_reject BOOLEAN,
                                                    listener_cancel BOOLEAN,
                                                    listener_delete BOOLEAN
);

CREATE TABLE IF NOT EXISTS request_types (
                                             id BIGSERIAL PRIMARY KEY,
                                             name VARCHAR(255) NOT NULL,
    effective_date DATE,
    expiration_date DATE,
    leave_policy_id BIGINT, -- Added leave_policy_id
    approval_config_id BIGINT UNIQUE,
    clubbing_config_id BIGINT UNIQUE,
    validation_config_id BIGINT UNIQUE,
    notification_config_id BIGINT UNIQUE,
    CONSTRAINT fk_approval_config FOREIGN KEY (approval_config_id) REFERENCES approval_configs(id),
    CONSTRAINT fk_clubbing_config FOREIGN KEY (clubbing_config_id) REFERENCES clubbing_configs(id),
    CONSTRAINT fk_validation_config FOREIGN KEY (validation_config_id) REFERENCES validation_configs(id),
    CONSTRAINT fk_notification_config FOREIGN KEY (notification_config_id) REFERENCES notification_configs(id),
    CONSTRAINT fk_leave_policy FOREIGN KEY (leave_policy_id) REFERENCES leave_policies(id) -- Added foreign key
    );