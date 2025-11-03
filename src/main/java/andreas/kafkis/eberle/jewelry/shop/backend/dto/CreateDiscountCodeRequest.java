package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDiscountCodeRequest {
    
    @NotBlank(message = "Discount code is required")
    private String code;
    
    private String description;
    
    @NotNull(message = "Discount type is required")
    private String discountType; // "PERCENTAGE" or "FIXED_AMOUNT"
    
    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;
    
    private BigDecimal minimumPurchaseAmount;
    
    private BigDecimal maximumDiscountAmount;
    
    private Integer usageLimit; // null = unlimited
    
    private Boolean isActive;
    
    private OffsetDateTime validFrom;
    
    private OffsetDateTime validUntil;
}


