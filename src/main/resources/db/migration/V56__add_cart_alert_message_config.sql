-- Add cart alert message configuration
-- V56: Add config for cart alert message (enable/disable and custom text)

-- Add cart alert message enabled config
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'cart.alert.enabled', 'true', 'Enable or disable the cart alert message in the cart drawer', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'cart.alert.enabled');

-- Add cart alert message text config
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'cart.alert.message', 'GET IT OR REGRET IT: These styles are going fast.', 'Custom text for the cart alert message', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'cart.alert.message');

