package andreas.kafkis.eberle.jewelry.shop.backend.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hero_slider_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeroSliderConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean isEnabled = false;

    @Column(name = "auto_play", nullable = false)
    @Builder.Default
    private boolean autoPlay = true;

    @Column(name = "slide_duration_seconds", nullable = false)
    @Builder.Default
    private Integer slideDurationSeconds = 5;

    @Column(name = "show_indicators", nullable = false)
    @Builder.Default
    private boolean showIndicators = true;

    @Column(name = "show_arrows", nullable = false)
    @Builder.Default
    private boolean showArrows = true;

    @Column(name = "transition_effect", length = 20)
    @Builder.Default
    private String transitionEffect = "fade"; // fade, slide, none

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
