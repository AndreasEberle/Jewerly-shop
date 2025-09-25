-- Create analytics_events table for tracking user behavior and business metrics
CREATE TABLE analytics_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    entity_type VARCHAR(50),
    user_ip VARCHAR(45),
    user_agent TEXT,
    referrer TEXT,
    session_id VARCHAR(255),
    additional_data TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes for better query performance
CREATE INDEX idx_analytics_events_event_type ON analytics_events(event_type);
CREATE INDEX idx_analytics_events_entity_id ON analytics_events(entity_id);
CREATE INDEX idx_analytics_events_created_at ON analytics_events(created_at);
CREATE INDEX idx_analytics_events_user_ip ON analytics_events(user_ip);
CREATE INDEX idx_analytics_events_session_id ON analytics_events(session_id);

-- Create composite index for common queries
CREATE INDEX idx_analytics_events_type_created ON analytics_events(event_type, created_at);
CREATE INDEX idx_analytics_events_entity_type_created ON analytics_events(entity_id, event_type, created_at);
