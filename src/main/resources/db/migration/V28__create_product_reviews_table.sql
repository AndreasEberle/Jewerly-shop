-- Create product_reviews table
CREATE TABLE product_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    is_approved BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, product_id)
);

-- Add indexes for better performance
CREATE INDEX idx_product_reviews_product_id ON product_reviews(product_id);
CREATE INDEX idx_product_reviews_user_id ON product_reviews(user_id);
CREATE INDEX idx_product_reviews_rating ON product_reviews(rating);
CREATE INDEX idx_product_reviews_created_at ON product_reviews(created_at);
CREATE INDEX idx_product_reviews_is_approved ON product_reviews(is_approved);

-- Add comments
COMMENT ON TABLE product_reviews IS 'Stores product reviews and ratings';
COMMENT ON COLUMN product_reviews.user_id IS 'Reference to the user who wrote the review';
COMMENT ON COLUMN product_reviews.product_id IS 'Reference to the reviewed product';
COMMENT ON COLUMN product_reviews.rating IS 'Rating from 1 to 5 stars';
COMMENT ON COLUMN product_reviews.title IS 'Review title';
COMMENT ON COLUMN product_reviews.content IS 'Review content/description';
COMMENT ON COLUMN product_reviews.is_verified_purchase IS 'Whether the reviewer actually purchased the product';
COMMENT ON COLUMN product_reviews.is_approved IS 'Whether the review is approved for display';
COMMENT ON COLUMN product_reviews.created_at IS 'When the review was created';
COMMENT ON COLUMN product_reviews.updated_at IS 'When the review was last updated';



