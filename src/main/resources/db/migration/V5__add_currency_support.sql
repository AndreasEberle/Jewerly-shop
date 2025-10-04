-- Add currency support and user preferences

-- Update products table to use CHF as base currency
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS base_currency VARCHAR(3) NOT NULL DEFAULT 'CHF';

-- Create currency rates table
CREATE TABLE IF NOT EXISTS currency_rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    rate DECIMAL(15,8) NOT NULL,
    markup_percentage DECIMAL(5,2) DEFAULT 5.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(from_currency, to_currency)
);

-- Create user preferences table
CREATE TABLE IF NOT EXISTS user_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    preferred_currency VARCHAR(3) NOT NULL DEFAULT 'CHF',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id)
);

-- Insert default currency rates (CHF as base)
INSERT INTO currency_rates (from_currency, to_currency, rate, markup_percentage) VALUES
    ('CHF', 'CHF', 1.00000000, 0.00),
    ('CHF', 'EUR', 0.95000000, 5.00),
    ('CHF', 'USD', 1.10000000, 5.00),
    ('CHF', 'JPY', 150.00000000, 5.00),
    ('CHF', 'GBP', 0.85000000, 5.00),
    ('CHF', 'CAD', 1.45000000, 5.00),
    ('CHF', 'AUD', 1.60000000, 5.00)
ON CONFLICT (from_currency, to_currency) DO NOTHING;

-- Insert reverse rates
INSERT INTO currency_rates (from_currency, to_currency, rate, markup_percentage) VALUES
    ('EUR', 'CHF', 1.05263158, 5.00),
    ('USD', 'CHF', 0.90909091, 5.00),
    ('JPY', 'CHF', 0.00666667, 5.00),
    ('GBP', 'CHF', 1.17647059, 5.00),
    ('CAD', 'CHF', 0.68965517, 5.00),
    ('AUD', 'CHF', 0.62500000, 5.00)
ON CONFLICT (from_currency, to_currency) DO NOTHING;

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_currency_rates_lookup ON currency_rates(from_currency, to_currency);
CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id);
