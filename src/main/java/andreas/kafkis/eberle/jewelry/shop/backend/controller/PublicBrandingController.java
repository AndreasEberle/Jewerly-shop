package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.BrandingConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.service.BrandingConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public/branding")
@Tag(name = "Public Branding", description = "Public APIs for branding configuration")
public class PublicBrandingController {

    private static final Logger log = LoggerFactory.getLogger(PublicBrandingController.class);

    @Autowired
    private BrandingConfigService brandingConfigService;

    @GetMapping("/config")
    @Operation(summary = "Get branding configuration for public display")
    public ResponseEntity<BrandingConfig> getConfig() {
        try {
            BrandingConfig config = brandingConfigService.getConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error getting branding config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
