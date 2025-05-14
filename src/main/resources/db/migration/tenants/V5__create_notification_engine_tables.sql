-- =============================================
-- Flyway Migration Script: Notification Engine
-- =============================================

-- 1. Table: notification_logs
CREATE TABLE IF NOT EXISTS notification_logs (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 notification_request_id VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255),
    channel VARCHAR(50) NOT NULL,
    recipient_address VARCHAR(512) NOT NULL,
    template_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    status_message TEXT,
    provider_message_id VARCHAR(255),
    attempt_count INT NOT NULL DEFAULT 1,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_payload JSONB,
    metadata JSONB
    );

-- Indexes for notification_logs
CREATE INDEX IF NOT EXISTS idx_notification_logs_user_id ON notification_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_logs_status ON notification_logs(status);
CREATE INDEX IF NOT EXISTS idx_notification_logs_channel ON notification_logs(channel);
CREATE INDEX IF NOT EXISTS idx_notification_logs_channel_status ON notification_logs(channel, status);
CREATE INDEX IF NOT EXISTS idx_notification_logs_created_at ON notification_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_notification_logs_recipient_address ON notification_logs(recipient_address);
CREATE INDEX IF NOT EXISTS idx_notification_logs_provider_message_id ON notification_logs(provider_message_id);

-- 2. Table: notification_templates
CREATE TABLE IF NOT EXISTS notification_templates (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      template_id VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    language VARCHAR(10) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255),

    CONSTRAINT uk_template_id_lang_version UNIQUE (template_id, language, version, channel)
    );

-- Index for quick lookup
CREATE INDEX IF NOT EXISTS idx_notification_templates_lookup
    ON notification_templates(template_id, channel, language, is_active, version DESC);

-- 3. Trigger Function for updated_at
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
DROP TRIGGER IF EXISTS update_notification_logs_updated_at ON notification_logs;
CREATE TRIGGER update_notification_logs_updated_at
    BEFORE UPDATE ON notification_logs
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

DROP TRIGGER IF EXISTS update_notification_templates_updated_at ON notification_templates;
CREATE TRIGGER update_notification_templates_updated_at
    BEFORE UPDATE ON notification_templates
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();
