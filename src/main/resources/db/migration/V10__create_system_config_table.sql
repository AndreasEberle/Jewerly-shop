-- Create system configuration table for dynamic settings
CREATE TABLE system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default configuration values
INSERT INTO system_config (config_key, config_value, description) VALUES
('storage.type', 'local', 'Storage type: local or s3'),
('storage.s3.bucket-name', '', 'S3 bucket name for file storage'),
('storage.s3.region', 'us-east-1', 'AWS S3 region'),
('storage.local.base-path', 'uploads', 'Local storage base path'),
('storage.public-base-url', 'http://localhost:8080/files/', 'Public base URL for file access'),
('email.enabled', 'true', 'Whether email notifications are enabled'),
('email.from', 'noreply@jewelryshop.com', 'Default from email address'),
('email.admin', 'admin@jewelryshop.com', 'Admin email address'),
('backup.enabled', 'true', 'Whether automated backups are enabled'),
('backup.method', 'jdbc', 'Backup method: jdbc or pg_dump'),
('maintenance.mode', 'false', 'Whether the application is in maintenance mode');

-- Create index for faster lookups
CREATE INDEX idx_system_config_key ON system_config(config_key);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_system_config_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_system_config_updated_at
    BEFORE UPDATE ON system_config
    FOR EACH ROW
    EXECUTE FUNCTION update_system_config_updated_at();
