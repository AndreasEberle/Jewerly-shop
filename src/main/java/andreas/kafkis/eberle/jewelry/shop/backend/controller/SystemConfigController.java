package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;

@RestController
@RequestMapping("/api/admin/system-config")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @GetMapping
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(configs);
    }
    
    @PostMapping
    public ResponseEntity<SystemConfig> createOrUpdateConfig(@RequestBody SystemConfig config) {
        systemConfigService.setConfigValue(
            config.getConfigKey(),
            config.getConfigValue(),
            config.getDescription()
        );
        
        SystemConfig savedConfig = systemConfigService.getConfigValue(config.getConfigKey()) != null ?
            systemConfigService.getAllConfigs().stream()
                .filter(c -> c.getConfigKey().equals(config.getConfigKey()))
                .findFirst()
                .orElse(null) : null;
        
        return ResponseEntity.ok(savedConfig);
    }
    
    @PutMapping("/{configKey}")
    public ResponseEntity<Map<String, String>> updateConfigValue(@PathVariable String configKey, @RequestBody Map<String, String> request) {
        System.out.println("PUT request received for configKey: " + configKey);
        System.out.println("Request body: " + request);
        
        String configValue = request.get("configValue");
        if (configValue == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "configValue is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            systemConfigService.setConfigValue(configKey, configValue, null);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Configuration updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error updating config: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update configuration: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{configKey}")
    public ResponseEntity<Map<String, String>> deleteConfig(@PathVariable String configKey) {
        systemConfigService.deleteConfig(configKey);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Configuration deleted successfully");
        return ResponseEntity.ok(response);
    }
}
