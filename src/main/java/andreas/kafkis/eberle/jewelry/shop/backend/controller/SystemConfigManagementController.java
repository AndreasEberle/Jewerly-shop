package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.SystemConfigRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "System Configuration Management", description = "Admin operations for system configuration management")
public class SystemConfigManagementController {
    
    private final SystemConfigRepository systemConfigRepository;
    private final SystemConfigService systemConfigService;
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all system configurations")
    public ResponseEntity<Page<SystemConfig>> getAllConfigs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "configKey") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        try {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<SystemConfig> configs = systemConfigRepository.findAll(pageable);
            return ResponseEntity.ok(configs);
            
        } catch (Exception e) {
            log.error("Error getting system configurations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/options/{configKey}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get configuration options for a specific key")
    public ResponseEntity<List<SystemConfig>> getConfigOptions(@PathVariable String configKey) {
        try {
            List<SystemConfig> options = systemConfigRepository.findAllByConfigKey(configKey);
            return ResponseEntity.ok(options);
            
        } catch (Exception e) {
            log.error("Error getting configuration options for key {}: {}", configKey, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system configuration by ID")
    public ResponseEntity<SystemConfig> getConfigById(@PathVariable UUID id) {
        try {
            SystemConfig config = systemConfigRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            log.error("Error getting system configuration: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update system configuration")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        try {
            SystemConfig config = systemConfigRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
            
            String newValue = request.get("configValue");
            if (newValue != null) {
                config.setConfigValue(newValue);
                systemConfigRepository.save(config);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Configuration updated successfully");
                response.put("config", config);
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "configValue is required");
                
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error updating system configuration: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update configuration: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new system configuration")
    public ResponseEntity<Map<String, Object>> createConfig(@RequestBody SystemConfig config) {
        try {
            SystemConfig savedConfig = systemConfigRepository.save(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Configuration created successfully");
            response.put("config", savedConfig);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating system configuration: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create configuration: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete system configuration")
    public ResponseEntity<Map<String, Object>> deleteConfig(@PathVariable UUID id) {
        try {
            SystemConfig config = systemConfigRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
            
            systemConfigRepository.delete(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Configuration deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting system configuration: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete configuration: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a system configuration by ID")
    public ResponseEntity<Map<String, Object>> activateConfig(@PathVariable UUID id) {
        try {
            SystemConfig activatedConfig = systemConfigService.activateConfig(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Configuration activated successfully");
            response.put("config", activatedConfig);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error activating system configuration with ID {}: {}", id, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to activate configuration: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a system configuration by key and value")
    public ResponseEntity<Map<String, Object>> activateConfigValue(@RequestParam String key, @RequestParam String value) {
        try {
            SystemConfig activatedConfig = systemConfigService.activateConfigValue(key, value);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Configuration activated successfully");
            response.put("config", activatedConfig);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error activating system configuration with key {} and value {}: {}", key, value, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to activate configuration: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
