-- Add dropdown values for configuration keys
-- V7: Add multiple values for configuration keys that should be dropdowns
-- NOTE: This migration requires V8 to run first to remove the unique constraint on config_key

-- First, add the category column to system_config table
ALTER TABLE system_config 
ADD COLUMN category VARCHAR(100);

-- Add index for better performance on category queries
CREATE INDEX idx_system_config_category ON system_config(category);

-- First, drop the existing unique constraint on config_key
ALTER TABLE system_config DROP CONSTRAINT IF EXISTS system_config_config_key_key;

-- Add a composite unique constraint on (config_key, config_value) to prevent duplicates
-- This allows multiple entries per config_key but prevents exact duplicates
ALTER TABLE system_config ADD CONSTRAINT system_config_key_value_unique 
    UNIQUE (config_key, config_value);

-- Add dropdown values for STORAGE_TYPE
-- Only insert if the combination doesn't already exist
INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'STORAGE_TYPE', 'local', 'Local file system storage', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'STORAGE_TYPE' AND config_value = 'local');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'STORAGE_TYPE', 's3', 'Amazon S3 cloud storage', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'STORAGE_TYPE' AND config_value = 's3');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'STORAGE_TYPE', 'hybrid', 'S3 primary with local fallback', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'STORAGE_TYPE' AND config_value = 'hybrid');

-- Add dropdown values for CURRENCY_BASE
INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'CURRENCY_BASE', 'CHF', 'Swiss Franc', 'currency', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'CURRENCY_BASE' AND config_value = 'CHF');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'CURRENCY_BASE', 'EUR', 'Euro', 'currency', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'CURRENCY_BASE' AND config_value = 'EUR');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'CURRENCY_BASE', 'USD', 'US Dollar', 'currency', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'CURRENCY_BASE' AND config_value = 'USD');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'CURRENCY_BASE', 'JPY', 'Japanese Yen', 'currency', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'CURRENCY_BASE' AND config_value = 'JPY');

-- Add dropdown values for LANGUAGE_DEFAULT
INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'LANGUAGE_DEFAULT', 'de-CH', 'German (Switzerland)', 'language', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'LANGUAGE_DEFAULT' AND config_value = 'de-CH');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'LANGUAGE_DEFAULT', 'en-US', 'English (United States)', 'language', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'LANGUAGE_DEFAULT' AND config_value = 'en-US');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'LANGUAGE_DEFAULT', 'fr-FR', 'French (France)', 'language', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'LANGUAGE_DEFAULT' AND config_value = 'fr-FR');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'LANGUAGE_DEFAULT', 'it-IT', 'Italian (Italy)', 'language', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'LANGUAGE_DEFAULT' AND config_value = 'it-IT');

-- Add dropdown values for SECURITY_2FA_ENABLED
INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'SECURITY_2FA_ENABLED', 'true', 'Two-factor authentication enabled', 'security', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'SECURITY_2FA_ENABLED' AND config_value = 'true');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'SECURITY_2FA_ENABLED', 'false', 'Two-factor authentication disabled', 'security', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'SECURITY_2FA_ENABLED' AND config_value = 'false');

-- Add dropdown values for S3_REGION
INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'S3_REGION', 'us-east-1', 'US East (N. Virginia)', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'S3_REGION' AND config_value = 'us-east-1');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'S3_REGION', 'us-west-2', 'US West (Oregon)', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'S3_REGION' AND config_value = 'us-west-2');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'S3_REGION', 'eu-west-1', 'Europe (Ireland)', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'S3_REGION' AND config_value = 'eu-west-1');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'S3_REGION', 'eu-central-1', 'Europe (Frankfurt)', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'S3_REGION' AND config_value = 'eu-central-1');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'S3_REGION', 'eu-north-1', 'Europe (Stockholm)', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'S3_REGION' AND config_value = 'eu-north-1');

INSERT INTO system_config (config_key, config_value, description, category, created_at, updated_at)
SELECT 'S3_REGION', 'ap-southeast-1', 'Asia Pacific (Singapore)', 'storage', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'S3_REGION' AND config_value = 'ap-southeast-1');
