package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.BackgroundImage;
import andreas.kafkis.eberle.jewelry.shop.backend.service.BackgroundImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/background-images")
@Tag(name = "Background Image Management", description = "APIs for managing shop background images")
public class BackgroundImageController {
    
    private static final Logger log = LoggerFactory.getLogger(BackgroundImageController.class);
    
    @Autowired
    private BackgroundImageService backgroundImageService;
    
    @GetMapping("/sections")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get available sections for background images")
    public ResponseEntity<List<String>> getAvailableSections() {
        try {
            List<String> sections = backgroundImageService.getAvailableSections();
            return ResponseEntity.ok(sections);
        } catch (Exception e) {
            log.error("Error getting available sections: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/mime-types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get allowed MIME types for background images")
    public ResponseEntity<List<String>> getAllowedMimeTypes() {
        try {
            List<String> mimeTypes = backgroundImageService.getAllowedMimeTypes();
            return ResponseEntity.ok(mimeTypes);
        } catch (Exception e) {
            log.error("Error getting allowed MIME types: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all background images")
    public ResponseEntity<List<BackgroundImage>> getAllBackgroundImages() {
        try {
            List<BackgroundImage> images = backgroundImageService.getAllBackgroundImages();
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error getting all background images: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all active background images")
    public ResponseEntity<List<BackgroundImage>> getActiveBackgroundImages() {
        try {
            List<BackgroundImage> images = backgroundImageService.getAllActiveBackgroundImages();
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error getting active background images: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/section/{sectionName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get background images for a specific section")
    public ResponseEntity<List<BackgroundImage>> getBackgroundImagesForSection(@PathVariable String sectionName) {
        try {
            List<BackgroundImage> images = backgroundImageService.getBackgroundImagesForSection(sectionName);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error getting background images for section {}: {}", sectionName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/section/{sectionName}/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active background image for a specific section")
    public ResponseEntity<BackgroundImage> getActiveBackgroundImageForSection(@PathVariable String sectionName) {
        try {
            return backgroundImageService.getActiveBackgroundImage(sectionName)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting active background image for section {}: {}", sectionName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload a new background image")
    public ResponseEntity<Map<String, Object>> uploadBackgroundImage(
            @RequestParam("sectionName") String sectionName,
            @RequestParam("file") MultipartFile file) {
        try {
            BackgroundImage image = backgroundImageService.uploadBackgroundImage(sectionName, file);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Background image uploaded successfully",
                "image", image
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid upload request: {}", e.getMessage());
            Map<String, Object> response = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            log.error("Error uploading background image: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to upload background image: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            log.error("Unexpected error uploading background image: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "An unexpected error occurred"
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a background image")
    public ResponseEntity<Map<String, Object>> activateBackgroundImage(@PathVariable UUID id) {
        try {
            BackgroundImage image = backgroundImageService.activateBackgroundImage(id);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Background image activated successfully",
                "image", image
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Error activating background image: {}", e.getMessage());
            Map<String, Object> response = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Unexpected error activating background image: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "An unexpected error occurred"
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a background image")
    public ResponseEntity<Map<String, Object>> deleteBackgroundImage(@PathVariable UUID id) {
        try {
            backgroundImageService.deleteBackgroundImage(id);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Background image deleted successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Error deleting background image: {}", e.getMessage());
            Map<String, Object> response = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Unexpected error deleting background image: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "An unexpected error occurred"
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/storage-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get storage configuration status")
    public ResponseEntity<Map<String, Object>> getStorageStatus() {
        try {
            boolean s3Configured = backgroundImageService.isS3Configured();
            String storageInfo = backgroundImageService.getStorageInfo();
            
            Map<String, Object> response = Map.of(
                "s3Configured", s3Configured,
                "storageInfo", storageInfo,
                "message", s3Configured ? "S3 is properly configured" : "S3 is not configured"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting storage status: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "s3Configured", false,
                "storageInfo", "Unknown",
                "message", "Failed to get storage status: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
