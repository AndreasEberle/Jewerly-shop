-- Create product_translations table for multi-language product data
-- V57: Add translations table for product name, description, and other translatable fields

CREATE TABLE IF NOT EXISTS product_translations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    material VARCHAR(100),
    special_offer_description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(product_id, language_code)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_product_translations_product_id ON product_translations(product_id);
CREATE INDEX IF NOT EXISTS idx_product_translations_language_code ON product_translations(language_code);

-- Add comment
COMMENT ON TABLE product_translations IS 'Stores translated versions of product data (name, description, etc.)';
COMMENT ON COLUMN product_translations.language_code IS 'ISO language code (e.g., en-US, de-DE, fr-FR)';

