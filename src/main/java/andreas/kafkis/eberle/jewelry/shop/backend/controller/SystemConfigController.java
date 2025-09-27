package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;

@RestController
@RequestMapping("/api/admin/system-config")
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * Get all system configurations
     */
    @GetMapping
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(configs);
    }

    /**
     * Get specific configuration by key
     */
    @GetMapping("/{configKey}")
    public ResponseEntity<SystemConfig> getConfig(@PathVariable String configKey) {
        Optional<SystemConfig> config = systemConfigService.getConfig(configKey);
        return config.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update configuration value
     */
    @PostMapping("/{configKey}")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable String configKey,
            @RequestBody Map<String, String> request) {
        
        String configValue = request.get("value");
        if (configValue == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Value is required"));
        }

        systemConfigService.updateConfigValue(configKey, configValue);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuration updated successfully",
                "configKey", configKey,
                "configValue", configValue
        ));
    }

    /**
     * Switch storage type (local <-> s3)
     */
    @PostMapping("/storage/switch")
    public ResponseEntity<Map<String, Object>> switchStorageType() {
        String currentType = systemConfigService.getStorageType();
        String newType = "local".equals(currentType) ? "s3" : "local";
        
        systemConfigService.setStorageType(newType);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Storage type switched successfully",
                "previousType", currentType,
                "newType", newType
        ));
    }

    /**
     * Toggle maintenance mode
     */
    @PostMapping("/maintenance/toggle")
    public ResponseEntity<Map<String, Object>> toggleMaintenanceMode() {
        boolean currentMode = systemConfigService.isMaintenanceMode();
        boolean newMode = !currentMode;
        
        systemConfigService.setMaintenanceMode(newMode);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Maintenance mode " + (newMode ? "enabled" : "disabled"),
                "maintenanceMode", newMode
        ));
    }

    /**
     * Get storage configuration summary
     */
    @GetMapping("/storage/summary")
    public ResponseEntity<Map<String, Object>> getStorageSummary() {
        Map<String, Object> summary = Map.of(
                "storageType", systemConfigService.getStorageType(),
                "s3BucketName", systemConfigService.getS3BucketName(),
                "s3Region", systemConfigService.getS3Region(),
                "localStoragePath", systemConfigService.getLocalStoragePath(),
                "publicBaseUrl", systemConfigService.getPublicBaseUrl()
        );
        
        return ResponseEntity.ok(summary);
    }
}
