package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SectionStyle;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.SectionStyleRepository;

@Service
public class SectionStyleService {
    
    private static final Logger log = LoggerFactory.getLogger(SectionStyleService.class);
    
    @Autowired
    private SectionStyleRepository sectionStyleRepository;
    
    public List<SectionStyle> getAllActiveStyles() {
        try {
            return sectionStyleRepository.findByIsActiveTrueOrderBySectionName();
        } catch (Exception e) {
            log.error("Error getting all active section styles: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public Optional<SectionStyle> getActiveStyleForSection(String sectionName) {
        try {
            return sectionStyleRepository.findBySectionNameAndIsActiveTrue(sectionName);
        } catch (Exception e) {
            log.error("Error getting active style for section {}: {}", sectionName, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    public List<SectionStyle> getStylesForSection(String sectionName) {
        try {
            return sectionStyleRepository.findBySectionNameOrderByCreatedAtDesc(sectionName);
        } catch (Exception e) {
            log.error("Error getting styles for section {}: {}", sectionName, e.getMessage(), e);
            return List.of();
        }
    }
    
    public SectionStyle createOrUpdateStyle(SectionStyle style) {
        try {
            // If this style is being set as active, deactivate all other styles for this section
            if (style.isActive()) {
                deactivateAllStylesForSection(style.getSectionName());
            }
            
            return sectionStyleRepository.save(style);
        } catch (Exception e) {
            log.error("Error creating/updating section style: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save section style", e);
        }
    }
    
    public SectionStyle activateStyle(UUID styleId) {
        try {
            SectionStyle style = sectionStyleRepository.findById(styleId)
                    .orElseThrow(() -> new RuntimeException("Section style not found with ID: " + styleId));
            
            // Deactivate all other styles for this section
            deactivateAllStylesForSection(style.getSectionName());
            
            // Activate this style
            style.setActive(true);
            return sectionStyleRepository.save(style);
        } catch (Exception e) {
            log.error("Error activating section style {}: {}", styleId, e.getMessage(), e);
            throw new RuntimeException("Failed to activate section style", e);
        }
    }
    
    public void deleteStyle(UUID styleId) {
        try {
            sectionStyleRepository.deleteById(styleId);
        } catch (Exception e) {
            log.error("Error deleting section style {}: {}", styleId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete section style", e);
        }
    }
    
    private void deactivateAllStylesForSection(String sectionName) {
        try {
            List<SectionStyle> existingStyles = sectionStyleRepository.findBySectionName(sectionName);
            for (SectionStyle existingStyle : existingStyles) {
                if (existingStyle.isActive()) {
                    existingStyle.setActive(false);
                    sectionStyleRepository.save(existingStyle);
                }
            }
        } catch (Exception e) {
            log.error("Error deactivating styles for section {}: {}", sectionName, e.getMessage(), e);
        }
    }
    
    public SectionStyle getDefaultStyleForSection(String sectionName) {
        // Return default styles based on section
        switch (sectionName.toLowerCase()) {
            case "hero":
                return SectionStyle.builder()
                        .sectionName(sectionName)
                        .backgroundColor("#f8fafc")
                        .textColor("#1f2937")
                        .overlayColor("#000000")
                        .overlayOpacity(0.3)
                        .backgroundSize("cover")
                        .backgroundPosition("center")
                        .backgroundRepeat("no-repeat")
                        .isActive(true)
                        .build();
            case "navigation":
                return SectionStyle.builder()
                        .sectionName(sectionName)
                        .backgroundColor("#ffffff")
                        .textColor("#374151")
                        .overlayColor("#000000")
                        .overlayOpacity(0.0)
                        .backgroundSize("cover")
                        .backgroundPosition("center")
                        .backgroundRepeat("no-repeat")
                        .isActive(true)
                        .build();
            case "featured_products":
                return SectionStyle.builder()
                        .sectionName(sectionName)
                        .backgroundColor("#f9fafb")
                        .textColor("#1f2937")
                        .overlayColor("#000000")
                        .overlayOpacity(0.1)
                        .backgroundSize("cover")
                        .backgroundPosition("center")
                        .backgroundRepeat("no-repeat")
                        .isActive(true)
                        .build();
            case "footer":
                return SectionStyle.builder()
                        .sectionName(sectionName)
                        .backgroundColor("#1f2937")
                        .textColor("#f9fafb")
                        .overlayColor("#000000")
                        .overlayOpacity(0.0)
                        .backgroundSize("cover")
                        .backgroundPosition("center")
                        .backgroundRepeat("no-repeat")
                        .isActive(true)
                        .build();
            default:
                return SectionStyle.builder()
                        .sectionName(sectionName)
                        .backgroundColor("#ffffff")
                        .textColor("#000000")
                        .overlayColor("#000000")
                        .overlayOpacity(0.0)
                        .backgroundSize("cover")
                        .backgroundPosition("center")
                        .backgroundRepeat("no-repeat")
                        .isActive(true)
                        .build();
        }
    }
}
