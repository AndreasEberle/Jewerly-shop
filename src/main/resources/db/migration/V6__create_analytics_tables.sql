-- Analytics Tables for Dashboard
-- V6: Create analytics tables for tracking user behavior, product views, and system metrics

-- User Analytics - Track user registrations and activity
CREATE TABLE user_analytics (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    event_type      VARCHAR(50) NOT NULL, -- 'registration', 'login', 'logout', 'profile_update'
    event_data      JSONB, -- Additional event-specific data
    ip_address      INET,
    user_agent      TEXT,
    country_code    VARCHAR(2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Product Analytics - Track product views, cart additions, etc.
CREATE TABLE product_analytics (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID REFERENCES products(id) ON DELETE CASCADE,
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL, -- NULL for anonymous users
    event_type      VARCHAR(50) NOT NULL, -- 'view', 'cart_add', 'cart_remove', 'wishlist_add'
    session_id      VARCHAR(255), -- For anonymous user tracking
    ip_address      INET,
    user_agent      TEXT,
    country_code    VARCHAR(2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Page Views - Track general page visits
CREATE TABLE page_views (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL, -- NULL for anonymous users
    page_path       VARCHAR(500) NOT NULL,
    page_title      VARCHAR(255),
    session_id      VARCHAR(255), -- For anonymous user tracking
    ip_address      INET,
    user_agent      TEXT,
    country_code    VARCHAR(2),
    referrer        TEXT,
    duration_seconds INTEGER, -- Time spent on page
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Orders Analytics - Track order events
CREATE TABLE order_analytics (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id        UUID, -- Reference to orders table (when created)
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    event_type      VARCHAR(50) NOT NULL, -- 'created', 'paid', 'shipped', 'delivered', 'cancelled'
    order_value     DECIMAL(10,2),
    currency        VARCHAR(3),
    ip_address      INET,
    user_agent      TEXT,
    country_code    VARCHAR(2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- System Metrics - Track system performance and usage
CREATE TABLE system_metrics (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_name     VARCHAR(100) NOT NULL,
    metric_value    DECIMAL(15,4) NOT NULL,
    metric_unit     VARCHAR(20), -- 'count', 'bytes', 'seconds', 'percentage'
    tags            JSONB, -- Additional tags for categorization
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX idx_user_analytics_user_id ON user_analytics(user_id);
CREATE INDEX idx_user_analytics_event_type ON user_analytics(event_type);
CREATE INDEX idx_user_analytics_created_at ON user_analytics(created_at);
CREATE INDEX idx_user_analytics_country ON user_analytics(country_code);

CREATE INDEX idx_product_analytics_product_id ON product_analytics(product_id);
CREATE INDEX idx_product_analytics_user_id ON product_analytics(user_id);
CREATE INDEX idx_product_analytics_event_type ON product_analytics(event_type);
CREATE INDEX idx_product_analytics_created_at ON product_analytics(created_at);
CREATE INDEX idx_product_analytics_session_id ON product_analytics(session_id);

CREATE INDEX idx_page_views_user_id ON page_views(user_id);
CREATE INDEX idx_page_views_page_path ON page_views(page_path);
CREATE INDEX idx_page_views_created_at ON page_views(created_at);
CREATE INDEX idx_page_views_session_id ON page_views(session_id);
CREATE INDEX idx_page_views_country ON page_views(country_code);

CREATE INDEX idx_order_analytics_user_id ON order_analytics(user_id);
CREATE INDEX idx_order_analytics_event_type ON order_analytics(event_type);
CREATE INDEX idx_order_analytics_created_at ON order_analytics(created_at);
CREATE INDEX idx_order_analytics_country ON order_analytics(country_code);

CREATE INDEX idx_system_metrics_name ON system_metrics(metric_name);
CREATE INDEX idx_system_metrics_created_at ON system_metrics(created_at);

-- Insert some initial system metrics
INSERT INTO system_metrics (metric_name, metric_value, metric_unit, tags, created_at) VALUES
('total_users', 0, 'count', '{"type": "counter"}', NOW()),
('total_products', 0, 'count', '{"type": "counter"}', NOW()),
('total_page_views', 0, 'count', '{"type": "counter"}', NOW()),
('total_orders', 0, 'count', '{"type": "counter"}', NOW()),
('avg_page_load_time', 0, 'seconds', '{"type": "gauge"}', NOW()),
('active_sessions', 0, 'count', '{"type": "gauge"}', NOW());


