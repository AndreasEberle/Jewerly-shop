package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    private String baseCurrency = "CHF";
    
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
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity = 0;
    
    private boolean active = true;
    
    // Special offer fields
    private boolean specialOffer = false;
    private BigDecimal specialOfferPrice;
    private String specialOfferDescription;
    
    // Relationships
    private Set<String> categories;
    private Set<String> tags;
}
