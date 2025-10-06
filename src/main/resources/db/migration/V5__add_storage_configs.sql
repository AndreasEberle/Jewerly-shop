-- Add S3 bucket name configuration
INSERT INTO system_config (config_key, config_value, description, created_at, updated_at)
VALUES (
    'S3_BUCKET_NAME', 
    'jewelry-shop-images', 
    'S3 bucket name for storing product images and files', 
    NOW(), 
    NOW()
) ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    description = EXCLUDED.description,
    updated_at = NOW();

-- Add local storage path configuration
INSERT INTO system_config (config_key, config_value, description, created_at, updated_at)
VALUES (
    'LOCAL_STORAGE_PATH', 
    'C:\\Users\\oarit\\OneDrive\\STS-WORKSPACES\\Jewelry shop\\uploads', 
    'Local file system path for storing uploaded files', 
    NOW(), 
    NOW()
) ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    description = EXCLUDED.description,
    updated_at = NOW();

-- Add S3 region configuration
INSERT INTO system_config (config_key, config_value, description, created_at, updated_at)
VALUES (
    'S3_REGION', 
    'eu-north-1', 
    'AWS S3 region for bucket operations', 
    NOW(), 
    NOW()
) ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    description = EXCLUDED.description,
    updated_at = NOW();

-- Add S3 access key configuration
INSERT INTO system_config (config_key, config_value, description, created_at, updated_at)
VALUES (
    'S3_ACCESS_KEY', 
    'AKIA5D75VAHX5WMQAPJX', 
    'AWS S3 access key for authentication', 
    NOW(), 
    NOW()
) ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    description = EXCLUDED.description,
    updated_at = NOW();

-- Add S3 secret key configuration
INSERT INTO system_config (config_key, config_value, description, created_at, updated_at)
VALUES (
    'S3_SECRET_KEY', 
    'ZIcZ6BN8nde4KGetrQ+d7KaNW8EunDqw27pp43ZD', 
    'AWS S3 secret key for authentication', 
    NOW(), 
    NOW()
) ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    description = EXCLUDED.description,
    updated_at = NOW();
