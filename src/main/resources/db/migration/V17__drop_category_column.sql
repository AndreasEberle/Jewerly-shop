-- Drop category column from system_config table since it's redundant with config_key
ALTER TABLE system_config DROP COLUMN IF EXISTS category;
