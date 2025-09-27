package andreas.kafkis.eberle.jewelry.shop.backend.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    
    private final UUID productId;
    private final int requestedQuantity;
    private final int availableQuantity;
    
    public InsufficientStockException(UUID productId, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for product %s. Requested: %d, Available: %d", 
                productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.requestedQuantity = 0;
        this.availableQuantity = 0;
    }
    
    public UUID getProductId() {
        return productId;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
