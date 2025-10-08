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

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SectionStyle;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SectionStyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public/section-styles")
@Tag(name = "Public Section Styles", description = "Public APIs for fetching section styles for the shop")
public class PublicSectionStyleController {
    
    private static final Logger log = LoggerFactory.getLogger(PublicSectionStyleController.class);
    
    @Autowired
    private SectionStyleService sectionStyleService;
    
    @GetMapping("/active")
    @Operation(summary = "Get all active section styles for the shop")
    public ResponseEntity<List<SectionStyle>> getActiveStyles() {
        try {
            List<SectionStyle> styles = sectionStyleService.getAllActiveStyles();
            log.info("Returning {} active section styles", styles.size());
            return ResponseEntity.ok(styles);
        } catch (Exception e) {
            log.error("Error getting active section styles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/section/{sectionName}")
    @Operation(summary = "Get active style for a specific section")
    public ResponseEntity<SectionStyle> getActiveStyleForSection(@PathVariable String sectionName) {
        try {
            Optional<SectionStyle> style = sectionStyleService.getActiveStyleForSection(sectionName);
            if (style.isPresent()) {
                log.info("Returning active style for section: {}", sectionName);
                return ResponseEntity.ok(style.get());
            } else {
                // Return default style if no custom style exists
                SectionStyle defaultStyle = sectionStyleService.getDefaultStyleForSection(sectionName);
                log.info("No custom style found for section {}, returning default", sectionName);
                return ResponseEntity.ok(defaultStyle);
            }
        } catch (Exception e) {
            log.error("Error getting active style for section {}: {}", sectionName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
