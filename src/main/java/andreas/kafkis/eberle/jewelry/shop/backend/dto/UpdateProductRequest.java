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
    
    @Size(max = 20, message = "Ring size must not exceed 20 characters")
    private String ringSize;
    
    @Size(max = 20, message = "Chain length must not exceed 20 characters")
    private String chainLength;
    
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;
    
    @Size(max = 50, message = "Finish must not exceed 50 characters")
    private String finish;
    
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
