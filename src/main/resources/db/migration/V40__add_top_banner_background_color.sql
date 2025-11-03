-- Add top banner background color configuration
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.top_banner.background_color', '#000000', 'Background color for the top banner (hex color code, e.g., #000000 for black)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.top_banner.background_color');

