-- Create user_favorites table
CREATE TABLE user_favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, product_id)
);

-- Add indexes for better performance
CREATE INDEX idx_user_favorites_user_id ON user_favorites(user_id);
CREATE INDEX idx_user_favorites_product_id ON user_favorites(product_id);
CREATE INDEX idx_user_favorites_created_at ON user_favorites(created_at);

-- Add comments
COMMENT ON TABLE user_favorites IS 'Stores user favorite products';
COMMENT ON COLUMN user_favorites.user_id IS 'Reference to the user who favorited the product';
COMMENT ON COLUMN user_favorites.product_id IS 'Reference to the favorited product';
COMMENT ON COLUMN user_favorites.created_at IS 'When the product was added to favorites';
