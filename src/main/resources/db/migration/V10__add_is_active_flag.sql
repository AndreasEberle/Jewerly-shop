-- Add is_active flag to system_config table
-- V10: Add is_active column and update existing data

-- Add is_active column
ALTER TABLE system_config ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT FALSE;

-- First, ensure only one value per key is active by setting all to false initially
UPDATE system_config SET is_active = FALSE;

-- Update the first entry for each config_key to be active
UPDATE system_config 
SET is_active = TRUE 
WHERE id IN (
    SELECT DISTINCT ON (config_key) id 
    FROM system_config 
    ORDER BY config_key, created_at ASC
);

-- For STORAGE_TYPE specifically, set 's3' as active (since hybrid is causing issues)
UPDATE system_config 
SET is_active = FALSE 
WHERE config_key = 'STORAGE_TYPE';

UPDATE system_config 
SET is_active = TRUE 
WHERE config_key = 'STORAGE_TYPE' AND config_value = 's3';

-- Add unique constraint to ensure only one active value per key
-- Use a partial unique index that only applies when is_active = true
CREATE UNIQUE INDEX uk_system_config_key_active_true 
ON system_config (config_key) 
WHERE is_active = true;
