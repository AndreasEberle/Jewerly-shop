-- Add configuration to enable/disable currency selector in navbar
-- Default is disabled (false) so users only see language selector
INSERT INTO system_config (config_key, config_value, description, is_active)
SELECT 'navbar.currency.enabled', 'false', 'Enable/disable currency selector icon in navbar. When disabled, users will only see the language selector.', true
WHERE NOT EXISTS (
    SELECT 1 FROM system_config 
    WHERE config_key = 'navbar.currency.enabled' 
    AND config_value = 'false'
);

