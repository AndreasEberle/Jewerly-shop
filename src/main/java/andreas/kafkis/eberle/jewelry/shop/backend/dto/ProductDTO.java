package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private UUID id;
    private String sku;
    private String slug;
    private String name;
    private String description;
    private BigDecimal price;
    private String baseCurrency;
    private String material;
    private String gemstone;
    private BigDecimal weightGrams;
    private String ringSize;
    private String chainLength;
    private String color;
    private String finish;
    private Integer quantity;
    private Integer availableQuantity; // Available after considering reservations
    private boolean active;
    private boolean showInFeatured;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Special offer fields
    private boolean specialOffer;
    private BigDecimal specialOfferPrice;
    private String specialOfferDescription;
    
    // Relationships
    private Set<String> categories;
    private Set<String> tags;
    private List<ProductImageDTO> images;
    
    // Helper methods
    public String getPrimaryImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.stream()
                    .filter(ProductImageDTO::isPrimary)
                    .findFirst()
                    .map(ProductImageDTO::getUrl)
                    .orElse(images.get(0).getUrl());
        }
        return null;
    }
    
    public BigDecimal getDisplayPrice() {
        return specialOffer && specialOfferPrice != null ? specialOfferPrice : price;
    }
    
    public boolean isOnSale() {
        return specialOffer && specialOfferPrice != null && specialOfferPrice.compareTo(price) < 0;
    }
}
