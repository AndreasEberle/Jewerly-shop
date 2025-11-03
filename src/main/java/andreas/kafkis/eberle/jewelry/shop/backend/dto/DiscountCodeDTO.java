package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscountCodeDTO {
    private UUID id;
    private String code;
    private String description;
    private String discountType; // "PERCENTAGE" or "FIXED_AMOUNT"
    private BigDecimal discountValue;
    private BigDecimal minimumPurchaseAmount;
    private BigDecimal maximumDiscountAmount;
    private Integer usageLimit;
    private Integer usageCount;
    private Boolean isActive;
    private OffsetDateTime validFrom;
    private OffsetDateTime validUntil;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}


