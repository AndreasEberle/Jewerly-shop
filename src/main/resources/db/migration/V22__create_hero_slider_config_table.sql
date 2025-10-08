-- Create hero_slider_config table for hero slider configuration
CREATE TABLE hero_slider_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    is_enabled BOOLEAN DEFAULT FALSE,
    auto_play BOOLEAN DEFAULT TRUE,
    slide_duration_seconds INTEGER DEFAULT 5,
    show_indicators BOOLEAN DEFAULT TRUE,
    show_arrows BOOLEAN DEFAULT TRUE,
    transition_effect VARCHAR(20) DEFAULT 'fade',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Insert default configuration
INSERT INTO hero_slider_config (is_enabled, auto_play, slide_duration_seconds, show_indicators, show_arrows, transition_effect) 
VALUES (FALSE, TRUE, 5, TRUE, TRUE, 'fade');
