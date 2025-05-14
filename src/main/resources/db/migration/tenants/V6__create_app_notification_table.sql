-- Flyway migration script for creating the app_notifications table
-- This table will be created within each tenant's schema.

CREATE TABLE IF NOT EXISTS app_notifications (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 user_id VARCHAR(255) NOT NULL,
    notification_request_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    message_body TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITHOUT TIME ZONE,
    action_url VARCHAR(2048),
    icon_url VARCHAR(2048),
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    additional_data JSONB
    );

-- Indexes for app_notifications table
CREATE INDEX IF NOT EXISTS idx_app_notifications_user_id_is_read_created_at
    ON app_notifications(user_id, is_read, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_app_notifications_notification_request_id
    ON app_notifications(notification_request_id);

CREATE INDEX IF NOT EXISTS idx_app_notifications_expires_at
    ON app_notifications(expires_at) WHERE expires_at IS NOT NULL;

-- (Optional) Trigger function to automatically update 'updated_at' if you add such a column
-- For now, AppNotification entity does not have an 'updated_at' column directly managed by triggers.
-- If you add it, you can reuse or create an update_modified_column trigger.
-- For example, if you add an updated_at column:
-- ALTER TABLE app_notifications ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
-- DROP TRIGGER IF EXISTS update_app_notifications_updated_at ON app_notifications;
-- CREATE TRIGGER update_app_notifications_updated_at
--     BEFORE UPDATE ON app_notifications
--     FOR EACH ROW
--     EXECUTE FUNCTION update_modified_column();
