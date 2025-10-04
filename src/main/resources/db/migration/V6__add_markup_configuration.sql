-- Add markup configuration system

-- Create system configuration for markup
INSERT INTO system_config (config_key, config_value, description) VALUES
    ('currency.markup.default', '5.0', 'Default markup percentage for currency conversions'),
    ('currency.markup.eur', '5.0', 'Markup percentage for EUR conversions'),
    ('currency.markup.usd', '7.0', 'Markup percentage for USD conversions'),
    ('currency.markup.jpy', '3.0', 'Markup percentage for JPY conversions'),
    ('currency.markup.gbp', '6.0', 'Markup percentage for GBP conversions'),
    ('currency.markup.cad', '5.5', 'Markup percentage for CAD conversions'),
    ('currency.markup.aud', '5.5', 'Markup percentage for AUD conversions')
ON CONFLICT (config_key) DO NOTHING;

-- Update existing currency rates with configurable markup
UPDATE currency_rates SET markup_percentage = 5.0 WHERE from_currency = 'CHF' AND to_currency = 'EUR';
UPDATE currency_rates SET markup_percentage = 7.0 WHERE from_currency = 'CHF' AND to_currency = 'USD';
UPDATE currency_rates SET markup_percentage = 3.0 WHERE from_currency = 'CHF' AND to_currency = 'JPY';
UPDATE currency_rates SET markup_percentage = 6.0 WHERE from_currency = 'CHF' AND to_currency = 'GBP';
UPDATE currency_rates SET markup_percentage = 5.5 WHERE from_currency = 'CHF' AND to_currency = 'CAD';
UPDATE currency_rates SET markup_percentage = 5.5 WHERE from_currency = 'CHF' AND to_currency = 'AUD';
