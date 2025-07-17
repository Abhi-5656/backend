-- V18__create_setup_wizard_tables.sql
-- This script creates the tables to manage the multi-step tenant setup wizard.

-- Main table to store the state of each setup wizard instance
CREATE TABLE IF NOT EXISTS setup_wizards (
                                             id BIGSERIAL PRIMARY KEY,
                                             company_logo TEXT,
                                             company_name VARCHAR(255),
    company_size VARCHAR(100),
    industry VARCHAR(100),
    company_address VARCHAR(512),
    compliance_region VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    subscription_id BIGINT,

    -- This foreign key links to the subscriptions table, which must be in the same schema.
    CONSTRAINT fk_wizard_to_subscription
    FOREIGN KEY (subscription_id)
    REFERENCES subscriptions(id)
    ON DELETE SET NULL
    );

-- Index for looking up wizards by their status
CREATE INDEX IF NOT EXISTS idx_setup_wizards_status ON setup_wizards(status);


-- Table to store the modules associated with each wizard instance
CREATE TABLE IF NOT EXISTS setup_wizard_modules (
                                                    wizard_id BIGINT NOT NULL,
                                                    module_name VARCHAR(100) NOT NULL,

    PRIMARY KEY (wizard_id, module_name),

    CONSTRAINT fk_modules_to_wizard
    FOREIGN KEY (wizard_id)
    REFERENCES setup_wizards(id)
    ON DELETE CASCADE
    );