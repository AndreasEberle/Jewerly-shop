-- Add additional user fields for better user management
ALTER TABLE users ADD COLUMN ldap_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN newsletter_subscribed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN marketing_emails BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN sms_notifications BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN preferred_language VARCHAR(10) DEFAULT 'en';
ALTER TABLE users ADD COLUMN timezone VARCHAR(50);
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN phone_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN profile_completed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN notes TEXT;

-- Add comments for documentation
COMMENT ON COLUMN users.ldap_enabled IS 'Whether user account is enabled for LDAP authentication';
COMMENT ON COLUMN users.newsletter_subscribed IS 'Whether user is subscribed to newsletter';
COMMENT ON COLUMN users.marketing_emails IS 'Whether user wants to receive marketing emails';
COMMENT ON COLUMN users.sms_notifications IS 'Whether user wants to receive SMS notifications';
COMMENT ON COLUMN users.preferred_language IS 'Users preferred language (en, de, fr, etc.)';
COMMENT ON COLUMN users.timezone IS 'Users timezone (e.g., Europe/Zurich)';
COMMENT ON COLUMN users.last_login_at IS 'Timestamp of last login';
COMMENT ON COLUMN users.email_verified IS 'Whether email address is verified';
COMMENT ON COLUMN users.phone_verified IS 'Whether phone number is verified';
COMMENT ON COLUMN users.profile_completed IS 'Whether user has completed their profile';
COMMENT ON COLUMN users.notes IS 'Admin notes about the user';
