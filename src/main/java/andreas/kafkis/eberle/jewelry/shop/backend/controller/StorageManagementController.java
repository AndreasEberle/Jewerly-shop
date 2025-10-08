package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/storage")
@Tag(name = "Storage Management", description = "APIs for managing storage operations")
public class StorageManagementController {
    
    private static final Logger log = LoggerFactory.getLogger(StorageManagementController.class);
    
    @Autowired
    private StorageService storageService;
    
    @DeleteMapping("/clear/{folderPath}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear all files from a specific folder")
    public ResponseEntity<Map<String, Object>> clearFolder(@PathVariable String folderPath) {
        try {
            log.info("Admin requested to clear folder: {}", folderPath);
            
            Map<String, Object> result = storageService.clearFolder(folderPath);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", result.get("message"),
                "result", result
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error clearing folder {}: {}", folderPath, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Failed to clear folder: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            log.error("Unexpected error clearing folder {}: {}", folderPath, e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "An unexpected error occurred: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/clear/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear all product images")
    public ResponseEntity<Map<String, Object>> clearProductImages() {
        return clearFolder("products");
    }
    
    @DeleteMapping("/clear/backgrounds")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear all background images")
    public ResponseEntity<Map<String, Object>> clearBackgroundImages() {
        return clearFolder("backgrounds");
    }
}
