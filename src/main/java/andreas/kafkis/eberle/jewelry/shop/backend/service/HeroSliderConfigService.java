package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.HeroSliderConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.HeroSliderConfigRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HeroSliderConfigService {

    @Autowired
    private HeroSliderConfigRepository heroSliderConfigRepository;

    public HeroSliderConfig getConfig() {
        return heroSliderConfigRepository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(() -> {
                    log.info("No hero slider config found, creating default config");
                    HeroSliderConfig defaultConfig = HeroSliderConfig.builder()
                            .isEnabled(false)
                            .autoPlay(true)
                            .slideDurationSeconds(5)
                            .showIndicators(true)
                            .showArrows(true)
                            .transitionEffect("fade")
                            .build();
                    return heroSliderConfigRepository.save(defaultConfig);
                });
    }

    @Transactional
    public HeroSliderConfig updateConfig(HeroSliderConfig config) {
        HeroSliderConfig existingConfig = getConfig();
        
        existingConfig.setEnabled(config.isEnabled());
        existingConfig.setAutoPlay(config.isAutoPlay());
        existingConfig.setSlideDurationSeconds(config.getSlideDurationSeconds());
        existingConfig.setShowIndicators(config.isShowIndicators());
        existingConfig.setShowArrows(config.isShowArrows());
        existingConfig.setTransitionEffect(config.getTransitionEffect());
        existingConfig.setUpdatedAt(LocalDateTime.now());
        
        HeroSliderConfig savedConfig = heroSliderConfigRepository.save(existingConfig);
        log.info("Updated hero slider config: enabled={}, autoPlay={}, duration={}s", 
                savedConfig.isEnabled(), savedConfig.isAutoPlay(), savedConfig.getSlideDurationSeconds());
        
        return savedConfig;
    }

    @Transactional
    public HeroSliderConfig toggleSlider() {
        HeroSliderConfig config = getConfig();
        config.setEnabled(!config.isEnabled());
        config.setUpdatedAt(LocalDateTime.now());
        
        HeroSliderConfig savedConfig = heroSliderConfigRepository.save(config);
        log.info("Toggled hero slider: enabled={}", savedConfig.isEnabled());
        
        return savedConfig;
    }
}
