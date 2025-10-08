package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.BrandingConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.service.BrandingConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/branding")
@Tag(name = "Branding Configuration", description = "APIs for managing shop branding")
public class BrandingConfigController {
    
    private static final Logger log = LoggerFactory.getLogger(BrandingConfigController.class);
    
    @Autowired
    private BrandingConfigService brandingConfigService;
    
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get branding configuration")
    public ResponseEntity<BrandingConfig> getConfig() {
        try {
            BrandingConfig config = brandingConfigService.getConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error getting branding config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update branding configuration")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody BrandingConfig config) {
        try {
            BrandingConfig updatedConfig = brandingConfigService.updateConfig(config);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Branding configuration updated successfully",
                "config", updatedConfig
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating branding config: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to update branding configuration: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset branding configuration to default")
    public ResponseEntity<Map<String, Object>> resetConfig() {
        try {
            BrandingConfig config = brandingConfigService.resetToDefault();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Branding configuration reset to default",
                "config", config
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resetting branding config: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to reset branding configuration: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
