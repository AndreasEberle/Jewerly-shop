package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private UUID id;
    private UUID orderId;
    private String orderNumber;
    private String paymentMethod;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String transactionId;
    private String gatewayResponse;
    private OffsetDateTime processedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String customerEmail;
    private String customerName;
    private String notes;
}


