-- Add discount color configuration to system_config
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
VALUES 
    ('discount_color', '#ef4444', 'Color for discount badges and special offer highlights', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('original_price_color', '#f97316', 'Color for original price strikethrough text', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
