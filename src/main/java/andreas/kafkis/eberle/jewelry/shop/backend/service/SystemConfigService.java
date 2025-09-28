package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.Optional;

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
        return systemConfigRepository.findByConfigKey(key);
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
     * Check if 2FA is globally enabled
     */
    public boolean is2FAEnabled() {
        return getBooleanConfig("2FA_ENABLED", false);
    }

    /**
     * Check if S3 storage is enabled
     */
    public boolean isS3StorageEnabled() {
        return getBooleanConfig("USE_S3_STORAGE", false);
    }

    /**
     * Check if debug mode is enabled
     */
    public boolean isDebugMode() {
        return getBooleanConfig("DEBUG_MODE", true);
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

    public String getS3BucketName() {
        return getConfig("S3_BUCKET_NAME")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("S3 bucket name not configured"));
    }

    public String getS3Region() {
        return getConfig("S3_REGION")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("S3 region not configured"));
    }

    public String getLocalStoragePath() {
        return getConfig("LOCAL_STORAGE_PATH")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("Local storage path not configured"));
    }

    public String getPublicBaseUrl() {
        return getConfig("PUBLIC_BASE_URL")
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new IllegalStateException("Public base URL not configured"));
    }

    public String getStorageType() {
        return getConfig("USE_S3_STORAGE")
                .map(config -> Boolean.parseBoolean(config.getConfigValue()) ? "S3" : "LOCAL")
                .orElseThrow(() -> new IllegalStateException("Storage type not configured"));
    }

    public void setStorageType(String storageType) {
        updateConfigValue("USE_S3_STORAGE", String.valueOf("S3".equalsIgnoreCase(storageType)));
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
        return systemConfigRepository.save(config);
    }

    /**
     * Update configuration value (alias for updateConfigValue)
     */
    @Transactional
    public SystemConfig updateConfig(String key, String value) {
        return updateConfigValue(key, value);
    }
}