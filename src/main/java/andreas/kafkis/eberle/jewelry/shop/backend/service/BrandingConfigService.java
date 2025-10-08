package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.BrandingConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.BrandingConfigRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BrandingConfigService {

    @Autowired
    private BrandingConfigRepository brandingConfigRepository;

    public BrandingConfig getConfig() {
        return brandingConfigRepository.findByIsActiveTrue()
                .orElseGet(() -> {
                    log.info("No active branding config found, creating default config");
                    BrandingConfig defaultConfig = BrandingConfig.builder()
                            .shopName("JewelryShop")
                            .shopNameFontFamily("Inter")
                            .shopNameFontSize(24)
                            .shopNameFontWeight("bold")
                            .shopNameFontStyle("normal")
                            .shopNameTextColor("#2563eb")
                            .shopNameTextDecoration("none")
                            .shopNameLetterSpacing(0.0)
                            .shopNameLineHeight(1.2)
                            .tagline("Premium Jewelry Collection")
                            .taglineFontSize(14)
                            .taglineTextColor("#6b7280")
                            .isActive(true)
                            .build();
                    return brandingConfigRepository.save(defaultConfig);
                });
    }

    @Transactional
    public BrandingConfig updateConfig(BrandingConfig config) {
        BrandingConfig existingConfig = getConfig();
        
        // Update all fields
        existingConfig.setLogoUrl(config.getLogoUrl());
        existingConfig.setLogoAltText(config.getLogoAltText());
        existingConfig.setLogoWidth(config.getLogoWidth());
        existingConfig.setLogoHeight(config.getLogoHeight());
        existingConfig.setFaviconUrl(config.getFaviconUrl());
        existingConfig.setFaviconType(config.getFaviconType());
        existingConfig.setFaviconSize(config.getFaviconSize());
        existingConfig.setShopName(config.getShopName());
        existingConfig.setShopNameFontFamily(config.getShopNameFontFamily());
        existingConfig.setShopNameFontSize(config.getShopNameFontSize());
        existingConfig.setShopNameFontWeight(config.getShopNameFontWeight());
        existingConfig.setShopNameFontStyle(config.getShopNameFontStyle());
        existingConfig.setShopNameTextColor(config.getShopNameTextColor());
        existingConfig.setShopNameTextDecoration(config.getShopNameTextDecoration());
        existingConfig.setShopNameLetterSpacing(config.getShopNameLetterSpacing());
        existingConfig.setShopNameLineHeight(config.getShopNameLineHeight());
        existingConfig.setTagline(config.getTagline());
        existingConfig.setTaglineFontSize(config.getTaglineFontSize());
        existingConfig.setTaglineTextColor(config.getTaglineTextColor());
        existingConfig.setUpdatedAt(LocalDateTime.now());
        
        BrandingConfig savedConfig = brandingConfigRepository.save(existingConfig);
        log.info("Updated branding config: shopName={}, logoUrl={}", 
                savedConfig.getShopName(), savedConfig.getLogoUrl());
        
        return savedConfig;
    }

    @Transactional
    public BrandingConfig resetToDefault() {
        BrandingConfig defaultConfig = BrandingConfig.builder()
                .shopName("JewelryShop")
                .shopNameFontFamily("Inter")
                .shopNameFontSize(24)
                .shopNameFontWeight("bold")
                .shopNameFontStyle("normal")
                .shopNameTextColor("#2563eb")
                .shopNameTextDecoration("none")
                .shopNameLetterSpacing(0.0)
                .shopNameLineHeight(1.2)
                .tagline("Premium Jewelry Collection")
                .taglineFontSize(14)
                .taglineTextColor("#6b7280")
                .faviconType("ico")
                .faviconSize(32)
                .isActive(true)
                .build();
        
        // Deactivate current config
        BrandingConfig currentConfig = getConfig();
        currentConfig.setActive(false);
        brandingConfigRepository.save(currentConfig);
        
        // Save new default config
        BrandingConfig savedConfig = brandingConfigRepository.save(defaultConfig);
        log.info("Reset branding config to default");
        
        return savedConfig;
    }
}
