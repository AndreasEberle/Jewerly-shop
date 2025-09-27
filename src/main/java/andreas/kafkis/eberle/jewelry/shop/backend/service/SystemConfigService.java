package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.SystemConfigRepository;

@Service
@Transactional
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    /**
     * Get configuration value by key with caching
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public String getConfigValue(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    /**
     * Get configuration value with default fallback
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * Get boolean configuration value
     */
    public boolean getBooleanConfigValue(String configKey, boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value);
    }

    /**
     * Update configuration value and clear cache
     */
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public void updateConfigValue(String configKey, String configValue) {
        if (systemConfigRepository.existsByConfigKey(configKey)) {
            systemConfigRepository.updateConfigValue(configKey, configValue);
        } else {
            // Create new config if it doesn't exist
            SystemConfig config = SystemConfig.builder()
                    .configKey(configKey)
                    .configValue(configValue)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            systemConfigRepository.save(config);
        }
    }

    /**
     * Get all configurations
     */
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    /**
     * Get configuration by key
     */
    public Optional<SystemConfig> getConfig(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey);
    }

    /**
     * Create new configuration
     */
    @CacheEvict(value = "systemConfig", allEntries = true)
    public SystemConfig createConfig(String key, String value, String description) {
        if (systemConfigRepository.findByConfigKey(key).isPresent()) {
            throw new IllegalArgumentException("Config key already exists: " + key);
        }
        SystemConfig newConfig = SystemConfig.builder()
                .configKey(key)
                .configValue(value)
                .description(description)
                .build();
        return systemConfigRepository.save(newConfig);
    }

    /**
     * Update configuration
     */
    @CacheEvict(value = "systemConfig", allEntries = true)
    public SystemConfig updateConfig(String key, String value) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Config key not found: " + key));
        config.setConfigValue(value);
        return systemConfigRepository.save(config);
    }

    /**
     * Delete configuration
     */
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void deleteConfig(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Config key not found: " + key));
        systemConfigRepository.delete(config);
    }

    /**
     * Storage-specific configuration methods
     */
    public String getStorageType() {
        return getConfigValue("storage.type", "local");
    }

    public void setStorageType(String storageType) {
        updateConfigValue("storage.type", storageType);
    }

    public String getS3BucketName() {
        return getConfigValue("storage.s3.bucket-name", "");
    }

    public String getS3Region() {
        return getConfigValue("storage.s3.region", "us-east-1");
    }

    public String getLocalStoragePath() {
        return getConfigValue("storage.local.base-path", "uploads");
    }

    public String getPublicBaseUrl() {
        return getConfigValue("storage.public-base-url", "http://localhost:8080/files/");
    }

    /**
     * Email-specific configuration methods
     */
    public boolean isEmailEnabled() {
        return getBooleanConfigValue("email.enabled", true);
    }

    public String getEmailFrom() {
        return getConfigValue("email.from", "noreply@jewelryshop.com");
    }

    public String getEmailAdmin() {
        return getConfigValue("email.admin", "admin@jewelryshop.com");
    }

    /**
     * Backup-specific configuration methods
     */
    public boolean isBackupEnabled() {
        return getBooleanConfigValue("backup.enabled", true);
    }

    public String getBackupMethod() {
        return getConfigValue("backup.method", "jdbc");
    }

    /**
     * Maintenance mode
     */
    public boolean isMaintenanceMode() {
        return getBooleanConfigValue("maintenance.mode", false);
    }

    public void setMaintenanceMode(boolean enabled) {
        updateConfigValue("maintenance.mode", String.valueOf(enabled));
    }
}
