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
@Table(name = "branding_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Logo configuration
    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "logo_alt_text", length = 100)
    private String logoAltText;

    @Column(name = "logo_width")
    private Integer logoWidth;

    @Column(name = "logo_height")
    private Integer logoHeight;

    // Favicon configuration
    @Column(name = "favicon_url")
    private String faviconUrl;

    @Column(name = "favicon_type", length = 20)
    private String faviconType; // ico, png, svg, etc.

    @Column(name = "favicon_size")
    private Integer faviconSize; // For reference, typically 16x16, 32x32, etc.

    // Shop name configuration
    @Column(name = "shop_name", length = 100)
    private String shopName;

    @Column(name = "shop_name_font_family", length = 50)
    private String shopNameFontFamily;

    @Column(name = "shop_name_font_size")
    private Integer shopNameFontSize;

    @Column(name = "shop_name_font_weight", length = 20)
    private String shopNameFontWeight;

    @Column(name = "shop_name_font_style", length = 20)
    private String shopNameFontStyle;

    @Column(name = "shop_name_text_color", length = 7)
    private String shopNameTextColor;

    @Column(name = "shop_name_text_decoration", length = 20)
    private String shopNameTextDecoration;

    @Column(name = "shop_name_letter_spacing")
    private Double shopNameLetterSpacing;

    @Column(name = "shop_name_line_height")
    private Double shopNameLineHeight;

    // Additional branding
    @Column(name = "tagline", length = 200)
    private String tagline;

    @Column(name = "tagline_font_size")
    private Integer taglineFontSize;

    @Column(name = "tagline_text_color", length = 7)
    private String taglineTextColor;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
