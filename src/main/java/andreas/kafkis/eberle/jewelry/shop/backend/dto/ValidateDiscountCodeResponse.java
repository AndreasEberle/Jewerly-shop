package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateDiscountCodeResponse {
    private boolean valid;
    private String message;
    private BigDecimal discountAmount;
    private String discountType;
    private BigDecimal originalAmount;
    private BigDecimal discountedAmount;
}


