-- Add products per row configuration for products page
-- Default value is 2
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'PRODUCTS_PER_ROW', '2', 'Number of products to display per row on the products page. Default: 2. Valid values: 1-6.', true, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_config 
    WHERE config_key = 'PRODUCTS_PER_ROW'
);

