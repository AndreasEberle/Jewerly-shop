-- Fix local storage path to be relative to application
UPDATE system_config 
SET config_value = 'uploads/products' 
WHERE config_key = 'LOCAL_STORAGE_PATH';

-- Add comment for documentation
COMMENT ON COLUMN system_config.config_value IS 'Updated to use relative path from application root';
