package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.HeroSliderConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.service.HeroSliderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public/hero-slider")
@Tag(name = "Public Hero Slider", description = "Public APIs for hero slider configuration")
public class PublicHeroSliderController {

    private static final Logger log = LoggerFactory.getLogger(PublicHeroSliderController.class);

    @Autowired
    private HeroSliderConfigService heroSliderConfigService;

    @GetMapping("/config")
    @Operation(summary = "Get hero slider configuration for public display")
    public ResponseEntity<HeroSliderConfig> getConfig() {
        try {
            HeroSliderConfig config = heroSliderConfigService.getConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error getting hero slider config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
