-- Create special_offer_descriptions table
CREATE TABLE IF NOT EXISTS special_offer_descriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert some default special offer descriptions (only if they don't exist)
INSERT INTO special_offer_descriptions (name, slug, description)
SELECT * FROM (VALUES
    ('Limited Time Offer', 'limited-time-offer', 'Special pricing for a limited time only'),
    ('Flash Sale', 'flash-sale', 'Quick sale with significant discounts'),
    ('Holiday Special', 'holiday-special', 'Special pricing for holiday season'),
    ('Clearance', 'clearance', 'Final clearance pricing'),
    ('Buy One Get One', 'buy-one-get-one', 'Buy one item and get another free'),
    ('Free Shipping', 'free-shipping', 'Complimentary shipping on this item'),
    ('New Arrival', 'new-arrival', 'Just arrived - be the first to own'),
    ('Best Seller', 'best-seller', 'Our most popular item'),
    ('Exclusive', 'exclusive', 'Exclusive offer for our customers'),
    ('Seasonal', 'seasonal', 'Special seasonal pricing')
) AS v(name, slug, description)
WHERE NOT EXISTS (SELECT 1 FROM special_offer_descriptions WHERE name = v.name);

-- Add default special offer description to system config (only if it doesn't exist)
INSERT INTO system_config (config_key, config_value, description, is_active)
SELECT 'default_special_offer_description', 'Limited Time Offer', 'Default special offer description when creating products', true
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'default_special_offer_description');
