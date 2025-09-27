package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Inventory;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.exception.InsufficientStockException;
import andreas.kafkis.eberle.jewelry.shop.backend.exception.ResourceNotFoundException;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.InventoryRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Get inventory for a specific product
     */
    @Transactional(readOnly = true)
    public Optional<Inventory> getInventoryByProductId(UUID productId) {
        return inventoryRepository.findById(productId);
    }

    /**
     * Get all inventory records
     */
    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    /**
     * Get available stock for a product (quantity - reserved)
     */
    @Transactional(readOnly = true)
    public int getAvailableStock(UUID productId) {
        return inventoryRepository.findById(productId)
                .map(inventory -> inventory.getQuantity() - inventory.getReserved())
                .orElse(0);
    }

    /**
     * Check if enough stock is available
     */
    @Transactional(readOnly = true)
    public boolean isStockAvailable(UUID productId, int requiredQuantity) {
        return getAvailableStock(productId) >= requiredQuantity;
    }

    /**
     * Add stock to inventory
     */
    public Inventory addStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseGet(() -> createInventoryForProduct(productId));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Remove stock from inventory
     */
    public Inventory removeStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(productId.toString()));

        int availableStock = inventory.getQuantity() - inventory.getReserved();
        if (availableStock < quantity) {
            throw new InsufficientStockException(productId, quantity, availableStock);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Set absolute stock quantity
     */
    public Inventory setStock(UUID productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseGet(() -> createInventoryForProduct(productId));

        // Check if new quantity would be less than reserved
        if (quantity < inventory.getReserved()) {
            throw new IllegalArgumentException("Cannot set stock lower than reserved amount: " + inventory.getReserved());
        }

        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Reserve stock for an order
     */
    public Inventory reserveStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(productId.toString()));

        int availableStock = inventory.getQuantity() - inventory.getReserved();
        if (availableStock < quantity) {
            throw new InsufficientStockException(productId, quantity, availableStock);
        }

        inventory.setReserved(inventory.getReserved() + quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Release reserved stock (cancel order or reservation)
     */
    public Inventory releaseReservedStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(productId.toString()));

        if (inventory.getReserved() < quantity) {
            throw new IllegalArgumentException("Cannot release more than reserved. Reserved: " + inventory.getReserved() + ", Requested: " + quantity);
        }

        inventory.setReserved(inventory.getReserved() - quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Fulfill reserved stock (complete order)
     */
    public Inventory fulfillReservedStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(productId.toString()));

        if (inventory.getReserved() < quantity) {
            throw new IllegalArgumentException("Cannot fulfill more than reserved. Reserved: " + inventory.getReserved() + ", Requested: " + quantity);
        }

        // Remove from both quantity and reserved
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setReserved(inventory.getReserved() - quantity);
        
        return inventoryRepository.save(inventory);
    }

    /**
     * Get products with low stock (below threshold)
     */
    @Transactional(readOnly = true)
    public List<Inventory> getLowStockProducts(int threshold) {
        return inventoryRepository.findLowStockProducts(threshold);
    }

    /**
     * Get products that are out of stock
     */
    @Transactional(readOnly = true)
    public List<Inventory> getOutOfStockProducts() {
        return inventoryRepository.findOutOfStockProducts();
    }

    /**
     * Create inventory record for a product
     */
    private Inventory createInventoryForProduct(UUID productId) {
        // Verify product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(productId.toString()));

        Inventory inventory = Inventory.builder()
                .productId(productId)
                .product(product)
                .quantity(0)
                .reserved(0)
                .build();

        return inventoryRepository.save(inventory);
    }

    /**
     * Bulk stock operations
     */
    public void bulkUpdateStock(List<StockUpdateRequest> updates) {
        for (StockUpdateRequest update : updates) {
            switch (update.getOperation()) {
                case ADD:
                    addStock(update.getProductId(), update.getQuantity());
                    break;
                case SET:
                    setStock(update.getProductId(), update.getQuantity());
                    break;
                case REMOVE:
                    removeStock(update.getProductId(), update.getQuantity());
                    break;
            }
        }
    }

    /**
     * Stock update request DTO
     */
    public static class StockUpdateRequest {
        private UUID productId;
        private int quantity;
        private StockOperation operation;

        public StockUpdateRequest() {}

        public StockUpdateRequest(UUID productId, int quantity, StockOperation operation) {
            this.productId = productId;
            this.quantity = quantity;
            this.operation = operation;
        }

        // Getters and setters
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public StockOperation getOperation() { return operation; }
        public void setOperation(StockOperation operation) { this.operation = operation; }
    }

    /**
     * Stock operation types
     */
    public enum StockOperation {
        ADD, REMOVE, SET
    }
}
