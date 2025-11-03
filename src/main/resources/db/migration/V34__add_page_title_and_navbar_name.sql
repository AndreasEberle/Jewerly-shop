-- Add page title and navbar name configuration to system_config
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at) VALUES
('site.page_title', 'JewelryShop - Premium Jewelry Collection', 'Page title displayed in browser tab', true, NOW(), NOW()),
('site.navbar_name', 'JewelryShop', 'Company/shop name displayed in navigation bar', true, NOW(), NOW())
ON CONFLICT (config_key) WHERE is_active = true DO NOTHING;

