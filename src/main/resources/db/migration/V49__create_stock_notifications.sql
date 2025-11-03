-- Create stock_notifications table for tracking when users request to be notified about product availability
DROP TABLE IF EXISTS stock_notifications CASCADE;

CREATE TABLE stock_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    email VARCHAR(255) NOT NULL, -- For guest users who aren't logged in
    notified BOOLEAN DEFAULT false, -- Whether user has been notified
    notified_at TIMESTAMP WITH TIME ZONE, -- When notification was sent
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create unique constraint to prevent duplicate requests
-- Use a partial unique index for user_id (when not null) and a separate constraint for email
CREATE UNIQUE INDEX idx_stock_notifications_user_product 
    ON stock_notifications(product_id, user_id) 
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX idx_stock_notifications_email_product 
    ON stock_notifications(product_id, email) 
    WHERE user_id IS NULL;

-- Create indexes for efficient queries
CREATE INDEX idx_stock_notifications_product ON stock_notifications(product_id);
CREATE INDEX idx_stock_notifications_user ON stock_notifications(user_id);
CREATE INDEX idx_stock_notifications_email ON stock_notifications(email);
CREATE INDEX idx_stock_notifications_notified ON stock_notifications(notified, notified_at);
CREATE INDEX idx_stock_notifications_created_at ON stock_notifications(created_at);

