-- Add top banner font size configuration
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.top_banner.font_size', '0.875rem', 'Font size for the top banner text (CSS value, e.g., 0.875rem, 1rem, 14px)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.top_banner.font_size');

