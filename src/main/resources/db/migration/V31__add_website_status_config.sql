-- Add website status configuration to system_config
INSERT INTO system_config (config_key, config_value, description, is_active) VALUES
('website_status', 'normal', 'Current website status: normal, construction, vacation', true),
('construction_message', 'We are currently working on improving our website. Please check back soon!', 'Message shown when website is under construction', false),
('vacation_message', 'We are currently on vacation and will be back soon!', 'Message shown when website is on vacation', false),
('vacation_start_date', '', 'Start date of vacation period (YYYY-MM-DD format)', false),
('vacation_end_date', '', 'End date of vacation period (YYYY-MM-DD format)', false),
('construction_image_url', '', 'URL of image to show during construction mode', false),
('vacation_image_url', '', 'URL of image to show during vacation mode', false);

-- Add comments
COMMENT ON COLUMN system_config.config_key IS 'Configuration key identifier';
COMMENT ON COLUMN system_config.config_value IS 'Configuration value';
COMMENT ON COLUMN system_config.description IS 'Human-readable description of the configuration';
COMMENT ON COLUMN system_config.is_active IS 'Whether this configuration is currently active';
