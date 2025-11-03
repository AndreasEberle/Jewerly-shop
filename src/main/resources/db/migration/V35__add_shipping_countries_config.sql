-- Add shipping countries configuration
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'shipping.countries.enabled', 'Switzerland,Liechtenstein', 'Comma-separated list of countries available for shipping', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'shipping.countries.enabled');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'shipping.countries.default', 'Switzerland', 'Default shipping country', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'shipping.countries.default');

