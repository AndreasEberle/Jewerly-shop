package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.SystemConfigRepository;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Transactional(readOnly = true)
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<SystemConfig> getConfig(String key) {
        return systemConfigRepository.findByConfigKeyAndIsActiveTrue(key);
    }
    
    @Transactional(readOnly = true)
    public List<SystemConfig> getAllActiveConfigs() {
        return systemConfigRepository.findAllByIsActiveTrue();
    }
    
    @Transactional(readOnly = true)
    public List<SystemConfig> getConfigOptions(String key) {
        return systemConfigRepository.findAllByConfigKey(key);
    }

    /**
     * Update configuration value
     */
    @Transactional
    public SystemConfig updateConfigValue(String key, String value) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("System config key not found: " + key));
        config.setConfigValue(value);
        return systemConfigRepository.save(config);
    }

    /**
     * Get configuration value as string
     */
    public String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    /**
     * Get configuration value as boolean
     */
    public boolean getBooleanConfig(String key, boolean defaultValue) {
        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value);
    }


    /**
     * Set configuration value
     */
    public void setConfigValue(String key, String value) {
        Optional<SystemConfig> existing = systemConfigRepository.findByConfigKey(key);
        if (existing.isPresent()) {
            SystemConfig config = existing.get();
            config.setConfigValue(value);
            systemConfigRepository.save(config);
        } else {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            systemConfigRepository.save(config);
        }
    }

    /**
     * Set boolean configuration value
     */
    public void setBooleanConfig(String key, boolean value) {
        setConfigValue(key, String.valueOf(value));
    }

    public boolean isMaintenanceMode() {
        return getConfig("MAINTENANCE_MODE")
                .map(config -> Boolean.parseBoolean(config.getConfigValue()))
                .orElse(false); // Default to false if not configured
    }

    public void setMaintenanceMode(boolean enabled) {
        updateConfigValue("MAINTENANCE_MODE", String.valueOf(enabled));
    }


    public String getPublicBaseUrl() {
        return getConfig("PUBLIC_BASE_URL")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("Public base URL not configured"));
    }

    public String getStorageType() {
        return getConfig("STORAGE_TYPE")
                .map(SystemConfig::getConfigValue)
                .orElse("s3"); // Default to S3 storage
    }

    public void setStorageType(String storageType) {
        // Validate storage type
        if (!storageType.equalsIgnoreCase("local") && 
            !storageType.equalsIgnoreCase("s3") && 
            !storageType.equalsIgnoreCase("hybrid")) {
            throw new IllegalArgumentException("Invalid storage type. Must be 'local', 's3', or 'hybrid'");
        }
        
        // Create or update the config
        SystemConfig config = systemConfigRepository.findByConfigKey("STORAGE_TYPE")
                .orElse(SystemConfig.builder()
                        .configKey("STORAGE_TYPE")
                        .configValue(storageType.toLowerCase())
                        .description("Storage type: local, s3, or hybrid")
                        .build());
        config.setConfigValue(storageType.toLowerCase());
        systemConfigRepository.save(config);
    }

    /**
     * Get S3 bucket name from configuration
     */
    public String getS3BucketName() {
        return getConfig("S3_BUCKET_NAME")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("S3 bucket name not configured"));
    }

    /**
     * Set S3 bucket name
     */
    public void setS3BucketName(String bucketName) {
        SystemConfig config = systemConfigRepository.findByConfigKey("S3_BUCKET_NAME")
                .orElse(SystemConfig.builder()
                        .configKey("S3_BUCKET_NAME")
                        .configValue(bucketName)
                        .description("S3 bucket name for storing product images and files")
                        .build());
        config.setConfigValue(bucketName);
        systemConfigRepository.save(config);
    }

    /**
     * Get S3 region from configuration
     */
    public String getS3Region() {
        return getConfig("S3_REGION")
                .map(SystemConfig::getConfigValue)
                .orElse("us-east-1"); // Default to us-east-1
    }

    /**
     * Set S3 region
     */
    public void setS3Region(String region) {
        SystemConfig config = systemConfigRepository.findByConfigKey("S3_REGION")
                .orElse(SystemConfig.builder()
                        .configKey("S3_REGION")
                        .configValue(region)
                        .description("AWS S3 region for bucket operations")
                        .build());
        config.setConfigValue(region);
        systemConfigRepository.save(config);
    }

    /**
     * Get local storage path from configuration
     */
    public String getLocalStoragePath() {
        return getConfig("LOCAL_STORAGE_PATH")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("Local storage path not configured"));
    }

    /**
     * Set local storage path
     */
    public void setLocalStoragePath(String path) {
        SystemConfig config = systemConfigRepository.findByConfigKey("LOCAL_STORAGE_PATH")
                .orElse(SystemConfig.builder()
                        .configKey("LOCAL_STORAGE_PATH")
                        .configValue(path)
                        .description("Local file system path for storing uploaded files")
                        .build());
        config.setConfigValue(path);
        systemConfigRepository.save(config);
    }

    /**
     * Get S3 access key from configuration
     */
    public String getS3AccessKey() {
        return getConfig("S3_ACCESS_KEY")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("S3 access key not configured"));
    }

    /**
     * Set S3 access key
     */
    public void setS3AccessKey(String accessKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey("S3_ACCESS_KEY")
                .orElse(SystemConfig.builder()
                        .configKey("S3_ACCESS_KEY")
                        .configValue(accessKey)
                        .description("AWS S3 access key for authentication")
                        .build());
        config.setConfigValue(accessKey);
        systemConfigRepository.save(config);
    }

    /**
     * Get S3 secret key from configuration
     */
    public String getS3SecretKey() {
        return getConfig("S3_SECRET_KEY")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("S3 secret key not configured"));
    }

    /**
     * Set S3 secret key
     */
    public void setS3SecretKey(String secretKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey("S3_SECRET_KEY")
                .orElse(SystemConfig.builder()
                        .configKey("S3_SECRET_KEY")
                        .configValue(secretKey)
                        .description("AWS S3 secret key for authentication")
                        .build());
        config.setConfigValue(secretKey);
        systemConfigRepository.save(config);
    }

    /**
     * Create a new configuration entry
     */
    @Transactional
    public SystemConfig createConfig(String key, String value, String description) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(description);
        config.setIsActive(false); // New entries are inactive by default
        return systemConfigRepository.save(config);
    }
    
    /**
     * Activate a specific configuration value by ID
     */
    @Transactional
    public SystemConfig activateConfig(UUID configId) {
        // First deactivate all configs with the same key
        SystemConfig targetConfig = systemConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with ID: " + configId));
        
        systemConfigRepository.deactivateAllByConfigKey(targetConfig.getConfigKey());
        
        // Then activate the target config
        systemConfigRepository.activateById(configId);
        
        return systemConfigRepository.findById(configId).orElse(targetConfig);
    }
    
    /**
     * Activate a configuration value by key and value
     */
    @Transactional
    public SystemConfig activateConfigValue(String key, String value) {
        // Find the config with the specific key and value
        SystemConfig targetConfig = systemConfigRepository.findAllByConfigKey(key)
                .stream()
                .filter(config -> value.equals(config.getConfigValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found with key: " + key + " and value: " + value));
        
        return activateConfig(targetConfig.getId());
    }

}