package andreas.kafkis.eberle.jewelry.shop.backend.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ResourceNotFoundException forProduct(String productId) {
        return new ResourceNotFoundException("Product not found with ID: " + productId);
    }
    
    public static ResourceNotFoundException forUser(String userId) {
        return new ResourceNotFoundException("User not found with ID: " + userId);
    }
    
    public static ResourceNotFoundException forOrder(String orderId) {
        return new ResourceNotFoundException("Order not found with ID: " + orderId);
    }
}
