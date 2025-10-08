package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    
    private UUID id;
    private String orderNumber;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;
    private String notes;
    private UserInfo customer;
    private AddressInfo shippingAddress;
    private AddressInfo billingAddress;
    private List<OrderItemInfo> items;
    private PaymentInfo payment;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressInfo {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String apartment;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemInfo {
        private UUID id;
        private UUID productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentInfo {
        private UUID id;
        private String paymentMethod;
        private String status;
        private BigDecimal amount;
        private String transactionId;
        private LocalDateTime processedAt;
    }
}
