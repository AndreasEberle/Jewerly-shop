-- Create background_images table for configurable shop backgrounds
CREATE TABLE background_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_name VARCHAR(50) NOT NULL, -- 'hero', 'navigation', 'footer', 'featured_products', etc.
    image_name VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    local_url VARCHAR(1024),
    s3_url VARCHAR(1024),
    storage_type VARCHAR(20) DEFAULT 'local',
    file_size BIGINT,
    mime_type VARCHAR(100),
    width INTEGER,
    height INTEGER,
    is_active BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    -- Ensure only one active image per section
    CONSTRAINT unique_active_per_section UNIQUE (section_name, is_active) DEFERRABLE INITIALLY DEFERRED
);

-- Create index for faster lookups
CREATE INDEX idx_background_images_section ON background_images(section_name);
CREATE INDEX idx_background_images_active ON background_images(section_name, is_active);

-- Add constraint to ensure only one active image per section
-- This will be enforced by the application logic
