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
    @Pattern(regexp = "^(CREATED|PAID|SHIPPED|DELIVERED|CANCELLED)$", 
             message = "Status must be one of: CREATED, PAID, SHIPPED, DELIVERED, CANCELLED")
    private String status;
    
    private String trackingNumber;
    private String carrier;
    private String notes;
}
