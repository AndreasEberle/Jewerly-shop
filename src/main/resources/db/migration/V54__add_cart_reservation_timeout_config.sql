-- Add cart reservation timeout configuration
-- Default value is 20 minutes
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'cart.reservation_timeout_minutes', '20', 'Cart reservation timeout in minutes. Default: 20. Items in cart are reserved for this duration before being released back to stock.', true, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_config 
    WHERE config_key = 'cart.reservation_timeout_minutes'
);

