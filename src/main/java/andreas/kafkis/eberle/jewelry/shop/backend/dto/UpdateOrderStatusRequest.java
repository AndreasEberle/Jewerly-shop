package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(PENDING|CONFIRMED|PROCESSING|SHIPPED|DELIVERED|CANCELLED|REFUNDED)$", 
             message = "Status must be one of: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED")
    private String status;
    
    private String trackingNumber;
    private String carrier;
    private String trackingLink;
    private Integer estimatedDeliveryDays;
    private String notes;
}





