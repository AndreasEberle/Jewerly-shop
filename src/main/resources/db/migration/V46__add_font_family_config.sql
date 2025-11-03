-- Add font family configuration for global typography
-- Default value is 'SyndicatGrotesk, Arial, Helvetica, sans-serif'
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'app.typography.fontFamily', 'SyndicatGrotesk, Arial, Helvetica, sans-serif', 'Global font family for the entire shop. This will be applied to the document root element.', true, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_config 
    WHERE config_key = 'app.typography.fontFamily'
);


