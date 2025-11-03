-- Create cart_reservations table for tracking reserved items with expiration
CREATE TABLE cart_reservations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id         UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity        INT NOT NULL CHECK (quantity > 0),
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (cart_id, product_id)
);

-- Create index for expiration cleanup
CREATE INDEX idx_cart_reservations_expires_at ON cart_reservations(expires_at);
CREATE INDEX idx_cart_reservations_cart_id ON cart_reservations(cart_id);
CREATE INDEX idx_cart_reservations_product_id ON cart_reservations(product_id);

-- Add reservation timeout configuration (default 20 minutes)
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'cart.reservation_timeout_minutes', '20', 'Number of minutes before cart reservations expire', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'cart.reservation_timeout_minutes');

