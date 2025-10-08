-- Add dual URL storage fields to product_images table
ALTER TABLE product_images 
ADD COLUMN local_url VARCHAR(1024),
ADD COLUMN s3_url VARCHAR(1024),
ADD COLUMN storage_type VARCHAR(20) DEFAULT 'local';

-- Add comments for documentation
COMMENT ON COLUMN product_images.local_url IS 'Local file system URL for the image';
COMMENT ON COLUMN product_images.s3_url IS 'S3 bucket URL for the image';
COMMENT ON COLUMN product_images.storage_type IS 'Storage type used: local, s3, or hybrid';

-- Update existing records to use the current url as local_url
UPDATE product_images 
SET local_url = url, storage_type = 'local' 
WHERE url IS NOT NULL;

-- Make the original url field nullable since we now have separate local and s3 URLs
ALTER TABLE product_images ALTER COLUMN url DROP NOT NULL;
