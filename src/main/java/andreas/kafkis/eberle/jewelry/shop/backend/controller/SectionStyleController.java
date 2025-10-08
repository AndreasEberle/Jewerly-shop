package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SectionStyle;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SectionStyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/section-styles")
@Tag(name = "Section Styles", description = "APIs for managing section styling")
public class SectionStyleController {
    
    private static final Logger log = LoggerFactory.getLogger(SectionStyleController.class);
    
    @Autowired
    private SectionStyleService sectionStyleService;
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all active section styles")
    public ResponseEntity<List<SectionStyle>> getActiveStyles() {
        try {
            List<SectionStyle> styles = sectionStyleService.getAllActiveStyles();
            return ResponseEntity.ok(styles);
        } catch (Exception e) {
            log.error("Error getting active section styles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/section/{sectionName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active style for a specific section")
    public ResponseEntity<SectionStyle> getActiveStyleForSection(@PathVariable String sectionName) {
        try {
            Optional<SectionStyle> style = sectionStyleService.getActiveStyleForSection(sectionName);
            if (style.isPresent()) {
                return ResponseEntity.ok(style.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting active style for section {}: {}", sectionName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/section/{sectionName}/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all styles for a specific section")
    public ResponseEntity<List<SectionStyle>> getStylesForSection(@PathVariable String sectionName) {
        try {
            List<SectionStyle> styles = sectionStyleService.getStylesForSection(sectionName);
            return ResponseEntity.ok(styles);
        } catch (Exception e) {
            log.error("Error getting styles for section {}: {}", sectionName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create or update a section style")
    public ResponseEntity<Map<String, Object>> createOrUpdateStyle(@RequestBody SectionStyle style) {
        try {
            SectionStyle savedStyle = sectionStyleService.createOrUpdateStyle(style);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Section style saved successfully",
                "style", savedStyle
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating/updating section style: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to save section style: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a section style")
    public ResponseEntity<Map<String, Object>> activateStyle(@PathVariable UUID id) {
        try {
            SectionStyle style = sectionStyleService.activateStyle(id);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Section style activated successfully",
                "style", style
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error activating section style {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to activate section style: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a section style")
    public ResponseEntity<Map<String, Object>> deleteStyle(@PathVariable UUID id) {
        try {
            sectionStyleService.deleteStyle(id);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Section style deleted successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting section style {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to delete section style: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
