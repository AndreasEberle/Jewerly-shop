-- Add language enabled/disabled configuration
-- V58: Add configs to enable/disable specific languages

-- Enable German (default)
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'language.de-DE.enabled', 'true', 'Enable or disable German (Germany) language', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'language.de-DE.enabled');

-- Enable English
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'language.en-US.enabled', 'true', 'Enable or disable English (United States) language', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'language.en-US.enabled');

-- Enable Japanese
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'language.ja-JP.enabled', 'true', 'Enable or disable Japanese (Japan) language', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'language.ja-JP.enabled');

-- Disable French (default to disabled)
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'language.fr-FR.enabled', 'false', 'Enable or disable French (France) language', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'language.fr-FR.enabled');

-- Disable Italian (default to disabled)
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'language.it-IT.enabled', 'false', 'Enable or disable Italian (Italy) language', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'language.it-IT.enabled');

