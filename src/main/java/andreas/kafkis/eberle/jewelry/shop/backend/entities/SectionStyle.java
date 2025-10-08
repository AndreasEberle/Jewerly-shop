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
@Table(name = "section_styles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionStyle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "section_name", nullable = false, length = 50)
    private String sectionName;
    
    @Column(name = "background_image_url", length = 1024)
    private String backgroundImageUrl;
    
    @Column(name = "background_color", length = 7)
    private String backgroundColor; // Hex color like #ffffff
    
    @Column(name = "text_color", length = 7)
    private String textColor; // Hex color like #000000
    
    @Column(name = "overlay_color", length = 7)
    private String overlayColor; // Hex color like #000000
    
    @Column(name = "overlay_opacity")
    private Double overlayOpacity; // 0.0 to 1.0
    
    @Column(name = "background_size", length = 20)
    @Builder.Default
    private String backgroundSize = "cover"; // cover, contain, auto
    
    @Column(name = "background_position", length = 20)
    @Builder.Default
    private String backgroundPosition = "center"; // center, top, bottom, left, right
    
    @Column(name = "background_repeat", length = 20)
    @Builder.Default
    private String backgroundRepeat = "no-repeat"; // no-repeat, repeat, repeat-x, repeat-y
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper method to get CSS background style
    public String getBackgroundStyle() {
        StringBuilder style = new StringBuilder();
        
        if (backgroundImageUrl != null && !backgroundImageUrl.isEmpty()) {
            style.append("background-image: url('").append(backgroundImageUrl).append("');");
        }
        
        if (backgroundColor != null && !backgroundColor.isEmpty()) {
            style.append("background-color: ").append(backgroundColor).append(";");
        }
        
        if (backgroundSize != null) {
            style.append("background-size: ").append(backgroundSize).append(";");
        }
        
        if (backgroundPosition != null) {
            style.append("background-position: ").append(backgroundPosition).append(";");
        }
        
        if (backgroundRepeat != null) {
            style.append("background-repeat: ").append(backgroundRepeat).append(";");
        }
        
        return style.toString();
    }
    
    // Helper method to get CSS text color style
    public String getTextStyle() {
        if (textColor != null && !textColor.isEmpty()) {
            return "color: " + textColor + ";";
        }
        return "";
    }
    
    // Helper method to get overlay style
    public String getOverlayStyle() {
        if (overlayColor != null && !overlayColor.isEmpty() && overlayOpacity != null) {
            return "background-color: " + overlayColor + "; opacity: " + overlayOpacity + ";";
        }
        return "";
    }
}
