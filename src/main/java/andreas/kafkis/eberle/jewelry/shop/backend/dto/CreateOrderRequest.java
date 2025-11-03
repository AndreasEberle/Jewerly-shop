package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    
    @NotNull(message = "Order items are required")
    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressRequest shippingAddress;
    
    @NotNull(message = "Billing address is required")
    @Valid
    private AddressRequest billingAddress;
    
    private String notes;
    
    private String discountCode;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private String productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressRequest {
        @NotNull(message = "Street is required")
        private String street;
        
        @NotNull(message = "City is required")
        private String city;
        
        @NotNull(message = "State is required")
        private String state;
        
        @NotNull(message = "Postal code is required")
        private String postalCode;
        
        @NotNull(message = "Country is required")
        private String country;
        
        private String apartment;
    }
}
