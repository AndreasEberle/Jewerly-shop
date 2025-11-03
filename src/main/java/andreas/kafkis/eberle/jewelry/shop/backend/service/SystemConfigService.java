package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.SystemConfigRepository;

@Service
public class SystemConfigService {
    
    private static final Logger log = LoggerFactory.getLogger(SystemConfigService.class);
    
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    
    public String getConfigValue(String configKey) {
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKeyAndIsActiveTrue(configKey);
        return config.map(SystemConfig::getConfigValue).orElse(null);
    }
    
    public void setConfigValue(String configKey, String configValue, String description) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKeyAndIsActiveTrue(configKey);
        
        if (existingConfig.isPresent()) {
            SystemConfig config = existingConfig.get();
            config.setConfigValue(configValue);
            config.setDescription(description);
            systemConfigRepository.save(config);
        } else {
            SystemConfig newConfig = SystemConfig.builder()
                    .configKey(configKey)
                    .configValue(configValue)
                    .description(description)
                    .isActive(true) // Set as active by default
                    .build();
            systemConfigRepository.save(newConfig);
        }
    }
    
    
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }
    
    public void deleteConfig(String configKey) {
        // Find the active configuration with this key and delete it
        Optional<SystemConfig> activeConfig = systemConfigRepository.findByConfigKeyAndIsActiveTrue(configKey);
        activeConfig.ifPresent(systemConfigRepository::delete);
    }
    
    public SystemConfig activateConfig(UUID id) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
        
        // Get the config key and value from the selected configuration
        String configKey = config.getConfigKey();
        String configValue = config.getConfigValue();
        
        log.info("Activating config: ID={}, Key={}, Value={}", id, configKey, configValue);
        
        // Find all configurations with the same key
        List<SystemConfig> allConfigsWithSameKey = systemConfigRepository.findAllByConfigKey(configKey);
        log.info("Found {} configs with key: {}", allConfigsWithSameKey.size(), configKey);
        
        // Deactivate all configurations with the same key
        for (SystemConfig sameKeyConfig : allConfigsWithSameKey) {
            log.info("Deactivating config: ID={}, Key={}, Value={}, Active={}", 
                    sameKeyConfig.getId(), sameKeyConfig.getConfigKey(), 
                    sameKeyConfig.getConfigValue(), sameKeyConfig.isActive());
            sameKeyConfig.setActive(false);
            systemConfigRepository.save(sameKeyConfig);
        }
        
        // Activate the selected configuration
        config.setActive(true);
        SystemConfig savedConfig = systemConfigRepository.save(config);
        log.info("Activated config: ID={}, Key={}, Value={}, Active={}", 
                savedConfig.getId(), savedConfig.getConfigKey(), 
                savedConfig.getConfigValue(), savedConfig.isActive());
        
        return savedConfig;
    }
    
    public SystemConfig activateConfigValue(String key, String value) {
        // Find the configuration with the specific key and value
        List<SystemConfig> configs = systemConfigRepository.findAllByConfigKey(key);
        Optional<SystemConfig> targetConfig = configs.stream()
                .filter(config -> value.equals(config.getConfigValue()))
                .findFirst();
        
        if (targetConfig.isPresent()) {
            // Deactivate all other configs with the same key
            configs.forEach(config -> {
                if (!config.getId().equals(targetConfig.get().getId())) {
                    config.setActive(false);
                    systemConfigRepository.save(config);
                }
            });
            
            // Activate the target config
            SystemConfig config = targetConfig.get();
            config.setActive(true);
            return systemConfigRepository.save(config);
        } else {
            throw new RuntimeException("Configuration not found with key: " + key + " and value: " + value);
        }
    }
    
    // Storage-related configuration methods
    public String getStorageType() {
        return getConfigValue("STORAGE_TYPE", "s3");
    }
    
    public String getLocalStoragePath() {
        return getConfigValue("LOCAL_STORAGE_PATH", "uploads/products");
    }
    
    public String getPublicBaseUrl() {
        return getConfigValue("storage.public.base.url", "http://localhost:8080/uploads/");
    }
    
    public String getS3BucketName() {
        return getConfigValue("S3_BUCKET_NAME", "jewelry-shop-images");
    }
    
    public String getS3Region() {
        return getConfigValue("S3_REGION", "eu-north-1");
    }
    
    public String getS3AccessKey() {
        return getConfigValue("S3_ACCESS_KEY", "");
    }
    
    public String getS3SecretKey() {
        return getConfigValue("S3_SECRET_KEY", "");
    }
    
    // Helper method to get config value with default
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }
}
