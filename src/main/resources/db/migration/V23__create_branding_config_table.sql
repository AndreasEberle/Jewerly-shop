-- Create branding_config table for shop branding configuration
CREATE TABLE branding_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    logo_url VARCHAR(1024),
    logo_alt_text VARCHAR(100),
    logo_width INTEGER,
    logo_height INTEGER,
    favicon_url VARCHAR(1024),
    favicon_type VARCHAR(20),
    favicon_size INTEGER,
    shop_name VARCHAR(100),
    shop_name_font_family VARCHAR(50),
    shop_name_font_size INTEGER,
    shop_name_font_weight VARCHAR(20),
    shop_name_font_style VARCHAR(20),
    shop_name_text_color VARCHAR(7),
    shop_name_text_decoration VARCHAR(20),
    shop_name_letter_spacing DOUBLE PRECISION,
    shop_name_line_height DOUBLE PRECISION,
    tagline VARCHAR(200),
    tagline_font_size INTEGER,
    tagline_text_color VARCHAR(7),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Insert default branding configuration
INSERT INTO branding_config (
    shop_name, 
    shop_name_font_family, 
    shop_name_font_size, 
    shop_name_font_weight, 
    shop_name_font_style, 
    shop_name_text_color, 
    shop_name_text_decoration, 
    shop_name_letter_spacing, 
    shop_name_line_height,
    tagline,
    tagline_font_size,
    tagline_text_color,
    favicon_type,
    favicon_size
) VALUES (
    'JewelryShop',
    'Inter',
    24,
    'bold',
    'normal',
    '#2563eb',
    'none',
    0.0,
    1.2,
    'Premium Jewelry Collection',
    14,
    '#6b7280',
    'ico',
    32
);
