-- Drop tables and indexes if they exist (in case of previous failed migration)
DROP TABLE IF EXISTS discount_code_usage CASCADE;
DROP TABLE IF EXISTS discount_codes CASCADE;

-- Drop indexes if they exist (cleanup from previous partial migration)
DROP INDEX IF EXISTS idx_discount_codes_code;
DROP INDEX IF EXISTS idx_discount_codes_active;
DROP INDEX IF EXISTS idx_discount_code_usage_code;
DROP INDEX IF EXISTS idx_discount_code_usage_user;

-- Create discount_codes table
CREATE TABLE discount_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    discount_value DECIMAL(10, 2) NOT NULL CHECK (discount_value > 0),
    minimum_purchase_amount DECIMAL(10, 2) DEFAULT 0,
    maximum_discount_amount DECIMAL(10, 2),
    usage_limit INTEGER,
    usage_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    valid_from TIMESTAMP WITH TIME ZONE,
    valid_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on code for fast lookup
CREATE INDEX idx_discount_codes_code ON discount_codes(code);
CREATE INDEX idx_discount_codes_active ON discount_codes(is_active, valid_from, valid_until);

-- Create discount_code_usage table to track which users have used which codes
CREATE TABLE discount_code_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    discount_code_id UUID NOT NULL REFERENCES discount_codes(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    order_id UUID REFERENCES orders(id) ON DELETE SET NULL,
    used_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    discount_amount DECIMAL(10, 2) NOT NULL
);

CREATE INDEX idx_discount_code_usage_code ON discount_code_usage(discount_code_id);
CREATE INDEX idx_discount_code_usage_user ON discount_code_usage(user_id);

