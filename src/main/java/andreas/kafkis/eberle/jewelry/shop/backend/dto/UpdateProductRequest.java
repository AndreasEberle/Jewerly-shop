package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;
    
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private BigDecimal price;
    
    private String baseCurrency;
    
    @Size(max = 100, message = "Material must not exceed 100 characters")
    private String material;
    
    @Size(max = 100, message = "Gemstone must not exceed 100 characters")
    private String gemstone;
    
    private BigDecimal weightGrams;
    
    private Integer quantity;
    
    private Boolean active;
    
    // Special offer fields
    private Boolean specialOffer;
    private BigDecimal specialOfferPrice;
    private String specialOfferDescription;
    
    // Relationships
    private Set<String> categories;
    private Set<String> tags;
}
