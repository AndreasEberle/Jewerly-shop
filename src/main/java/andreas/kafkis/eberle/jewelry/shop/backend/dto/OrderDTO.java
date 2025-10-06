package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private String customerEmail;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private String shippingAddress;
    private String billingAddress;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<OrderItemDTO> items;
    private PaymentDTO payment;
    private String trackingNumber;
    private String carrier;
}

