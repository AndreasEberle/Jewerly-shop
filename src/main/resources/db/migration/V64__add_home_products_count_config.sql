-- Add home products count configuration
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'HOME_PRODUCTS_COUNT', '4', 'Number of products to display in the home product grid (2-4)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'HOME_PRODUCTS_COUNT');

