package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.BackgroundImage;
import andreas.kafkis.eberle.jewelry.shop.backend.service.BackgroundImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public/background-images")
@Tag(name = "Public Background Images", description = "Public APIs for fetching background images for the shop")
public class PublicBackgroundImageController {
    
    private static final Logger log = LoggerFactory.getLogger(PublicBackgroundImageController.class);
    
    @Autowired
    private BackgroundImageService backgroundImageService;
    
    @GetMapping("/active")
    @Operation(summary = "Get all active background images for the shop")
    public ResponseEntity<List<BackgroundImage>> getActiveBackgroundImages() {
        try {
            List<BackgroundImage> images = backgroundImageService.getAllActiveBackgroundImages();
            log.info("Returning {} active background images", images.size());
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error getting active background images: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/section/{sectionName}")
    @Operation(summary = "Get all background images for a specific section")
    public ResponseEntity<List<BackgroundImage>> getBackgroundImagesForSection(@PathVariable String sectionName) {
        try {
            List<BackgroundImage> images = backgroundImageService.getBackgroundImagesForSection(sectionName);
            log.info("Returning {} background images for section: {}", images.size(), sectionName);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error getting background images for section {}: {}", sectionName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/section/{sectionName}/active")
    @Operation(summary = "Get active background image for a specific section")
    public ResponseEntity<BackgroundImage> getActiveBackgroundImageForSection(@PathVariable String sectionName) {
        try {
            Optional<BackgroundImage> image = backgroundImageService.getActiveBackgroundImage(sectionName);
            if (image.isPresent()) {
                log.info("Returning active background image for section: {}", sectionName);
                return ResponseEntity.ok(image.get());
            } else {
                log.info("No active background image found for section: {}", sectionName);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting active background image for section {}: {}", sectionName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
