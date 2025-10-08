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

import andreas.kafkis.eberle.jewelry.shop.backend.entities.HeroSliderConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.service.HeroSliderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/hero-slider")
@Tag(name = "Hero Slider Configuration", description = "APIs for managing hero slider settings")
public class HeroSliderConfigController {
    
    private static final Logger log = LoggerFactory.getLogger(HeroSliderConfigController.class);
    
    @Autowired
    private HeroSliderConfigService heroSliderConfigService;
    
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get hero slider configuration")
    public ResponseEntity<HeroSliderConfig> getConfig() {
        try {
            HeroSliderConfig config = heroSliderConfigService.getConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error getting hero slider config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update hero slider configuration")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody HeroSliderConfig config) {
        try {
            HeroSliderConfig updatedConfig = heroSliderConfigService.updateConfig(config);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Hero slider configuration updated successfully",
                "config", updatedConfig
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating hero slider config: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to update hero slider configuration: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle hero slider on/off")
    public ResponseEntity<Map<String, Object>> toggleSlider() {
        try {
            HeroSliderConfig config = heroSliderConfigService.toggleSlider();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Hero slider " + (config.isEnabled() ? "enabled" : "disabled"),
                "config", config
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling hero slider: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to toggle hero slider: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
