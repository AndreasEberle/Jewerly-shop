-- Create section_styles table for comprehensive section styling
CREATE TABLE section_styles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_name VARCHAR(50) NOT NULL,
    background_image_url VARCHAR(1024),
    background_color VARCHAR(7), -- Hex color like #ffffff
    text_color VARCHAR(7), -- Hex color like #000000
    overlay_color VARCHAR(7), -- Hex color like #000000
    overlay_opacity DECIMAL(3,2) CHECK (overlay_opacity >= 0.0 AND overlay_opacity <= 1.0),
    background_size VARCHAR(20) DEFAULT 'cover',
    background_position VARCHAR(20) DEFAULT 'center',
    background_repeat VARCHAR(20) DEFAULT 'no-repeat',
    is_active BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create index for section name lookups
CREATE INDEX idx_section_styles_section_name ON section_styles(section_name);

-- Create unique constraint to ensure only one active style per section
CREATE UNIQUE INDEX idx_section_styles_active_section ON section_styles(section_name) WHERE is_active = true;

-- Insert default styles for main sections
INSERT INTO section_styles (section_name, background_color, text_color, overlay_color, overlay_opacity, is_active) VALUES
('hero', '#f8fafc', '#1f2937', '#000000', 0.3, true),
('navigation', '#ffffff', '#374151', '#000000', 0.0, true),
('featured_products', '#f9fafb', '#1f2937', '#000000', 0.1, true),
('footer', '#1f2937', '#f9fafb', '#000000', 0.0, true);
