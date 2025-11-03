-- Add top banner (shipping message) configuration
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.top_banner.enabled', 'true', 'Enable or disable the top banner above navbar', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.top_banner.enabled');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.top_banner.text', 'Free shipping on all intl. orders $150+', 'Text to display in the top banner', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.top_banner.text');

