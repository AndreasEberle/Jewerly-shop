-- Add newsletter subscription configuration to footer
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'footer.newsletter.enabled', 'true', 'Enable or disable newsletter subscription section in footer', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'footer.newsletter.enabled');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'footer.newsletter.title', 'Subscribe to Our Newsletter', 'Title for newsletter subscription section', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'footer.newsletter.title');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'footer.newsletter.description', 'Get the latest updates on new products and upcoming sales.', 'Description for newsletter subscription section', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'footer.newsletter.description');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'footer.newsletter.button_text', 'Subscribe', 'Button text for newsletter subscription', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'footer.newsletter.button_text');


