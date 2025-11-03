package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Cart;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.CartReservation;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Inventory;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CartRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CartReservationRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.InventoryRepository;

@Service
public class CartReservationService {
    
    private static final Logger log = LoggerFactory.getLogger(CartReservationService.class);
    
    @Autowired
    private CartReservationRepository cartReservationRepository;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private CartRepository cartRepository;
    
    /**
     * Get reservation timeout in minutes (default 20)
     */
    private int getReservationTimeoutMinutes() {
        String timeoutStr = systemConfigService.getConfigValue("cart.reservation_timeout_minutes");
        try {
            return timeoutStr != null ? Integer.parseInt(timeoutStr) : 20;
        } catch (NumberFormatException e) {
            log.warn("Invalid reservation timeout value: {}, using default 20 minutes", timeoutStr);
            return 20;
        }
    }
    
    /**
     * Create or update reservation for a cart item
     */
    @Transactional
    public CartReservation reserveStockForCart(Cart cart, Product product, int quantity) {
        // First, clean up expired reservations for this product
        releaseExpiredReservationsForProduct(product.getId());
        
        // Check available stock (quantity - reserved in inventory - reserved in carts)
        Inventory inventory = inventoryRepository.findById(product.getId())
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + product.getId()));
        
        int reservedInCarts = sumReservedQuantityForProduct(product.getId());
        int availableStock = inventory.getQuantity() - inventory.getReserved() - reservedInCarts;
        
        if (availableStock < quantity) {
            throw new RuntimeException(
                String.format("Insufficient stock. Available: %d, Requested: %d", availableStock, quantity)
            );
        }
        
        // Find existing reservation for this cart+product
        Optional<CartReservation> existingReservation = 
            cartReservationRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        
        int timeoutMinutes = getReservationTimeoutMinutes();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(timeoutMinutes);
        
        if (existingReservation.isPresent()) {
            // Update existing reservation
            CartReservation reservation = existingReservation.get();
            reservation.setQuantity(quantity);
            reservation.setExpiresAt(expiresAt);
            return cartReservationRepository.save(reservation);
        } else {
            // Create new reservation - explicitly set createdAt
            CartReservation reservation = CartReservation.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .expiresAt(expiresAt)
                    .createdAt(OffsetDateTime.now())
                    .build();
            return cartReservationRepository.save(reservation);
        }
    }
    
    /**
     * Release reservation for a cart item
     */
    @Transactional
    public void releaseReservation(UUID cartId, UUID productId) {
        cartReservationRepository.findByCartIdAndProductId(cartId, productId)
                .ifPresent(cartReservationRepository::delete);
    }
    
    /**
     * Release all reservations for a cart
     */
    @Transactional
    public void releaseAllReservationsForCart(UUID cartId) {
        List<CartReservation> reservations = cartReservationRepository.findByCartId(cartId);
        cartReservationRepository.deleteAll(reservations);
    }
    
    /**
     * Get total reserved quantity for a product (from all active cart reservations)
     */
    public int sumReservedQuantityForProduct(UUID productId) {
        Integer sum = cartReservationRepository.sumReservedQuantityForProduct(productId, OffsetDateTime.now());
        return sum != null ? sum : 0;
    }
    
    /**
     * Get total reserved quantity for a product, excluding a specific cart
     * This is used when converting cart to order - we exclude the user's own cart
     */
    public int sumReservedQuantityForProductExcludingCart(UUID productId, UUID excludeCartId) {
        Integer sum = cartReservationRepository.sumReservedQuantityForProductExcludingCart(productId, OffsetDateTime.now(), excludeCartId);
        return sum != null ? sum : 0;
    }
    
    /**
     * Release expired reservations for a specific product
     */
    @Transactional
    public void releaseExpiredReservationsForProduct(UUID productId) {
        List<CartReservation> expired = cartReservationRepository.findExpiredReservations(OffsetDateTime.now())
                .stream()
                .filter(r -> r.getProduct().getId().equals(productId))
                .toList();
        
        if (!expired.isEmpty()) {
            cartReservationRepository.deleteAll(expired);
            log.info("Released {} expired reservations for product {}", expired.size(), productId);
        }
    }
    
    /**
     * Scheduled task to clean up expired reservations every minute
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void cleanupExpiredReservations() {
        OffsetDateTime now = OffsetDateTime.now();
        List<CartReservation> expired = cartReservationRepository.findExpiredReservations(now);
        
        if (!expired.isEmpty()) {
            int count = expired.size();
            cartReservationRepository.deleteAll(expired);
            log.info("Cleaned up {} expired cart reservations", count);
        }
    }
    
    /**
     * Get available stock for a product (considering cart reservations)
     */
    public int getAvailableStock(UUID productId) {
        Inventory inventory = inventoryRepository.findById(productId).orElse(null);
        if (inventory == null) {
            return 0;
        }
        
        int reservedInCarts = sumReservedQuantityForProduct(productId);
        return Math.max(0, inventory.getQuantity() - inventory.getReserved() - reservedInCarts);
    }
    
    /**
     * Get available stock for a product, excluding a specific cart
     * Used when converting cart to order - excludes user's own cart reservations
     */
    public int getAvailableStockExcludingCart(UUID productId, UUID excludeCartId) {
        Inventory inventory = inventoryRepository.findById(productId).orElse(null);
        if (inventory == null) {
            return 0;
        }
        
        int reservedInCarts = sumReservedQuantityForProductExcludingCart(productId, excludeCartId);
        return Math.max(0, inventory.getQuantity() - inventory.getReserved() - reservedInCarts);
    }
}

