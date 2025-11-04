package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository productRepository;
    
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
        
        CartReservation reservation;
        if (existingReservation.isPresent()) {
            // Update existing reservation
            reservation = existingReservation.get();
            reservation.setQuantity(quantity);
            reservation.setExpiresAt(expiresAt);
            reservation = cartReservationRepository.save(reservation);
        } else {
            // Create new reservation - explicitly set createdAt
            reservation = CartReservation.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .expiresAt(expiresAt)
                    .createdAt(OffsetDateTime.now())
                    .build();
            reservation = cartReservationRepository.save(reservation);
        }
        
        // Extend expiration for ALL reservations in this cart (prolong cart lifetime)
        extendCartReservations(cart.getId());
        
        return reservation;
    }
    
    /**
     * Extend expiration time for all reservations in a cart
     * This prolongs the cart lifetime whenever a new item is added or quantity is updated
     */
    @Transactional
    public void extendCartReservations(UUID cartId) {
        int timeoutMinutes = getReservationTimeoutMinutes();
        OffsetDateTime newExpiresAt = OffsetDateTime.now().plusMinutes(timeoutMinutes);
        
        List<CartReservation> reservations = cartReservationRepository.findByCartId(cartId);
        for (CartReservation reservation : reservations) {
            reservation.setExpiresAt(newExpiresAt);
            cartReservationRepository.save(reservation);
        }
        
        if (!reservations.isEmpty()) {
            log.debug("Extended expiration for {} reservations in cart {}", reservations.size(), cartId);
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
     * Also removes cart items when their reservations expire
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void cleanupExpiredReservations() {
        OffsetDateTime now = OffsetDateTime.now();
        List<CartReservation> expired = cartReservationRepository.findExpiredReservations(now);
        
        if (!expired.isEmpty()) {
            // Group expired reservations by cart
            Map<UUID, List<CartReservation>> expiredByCart = expired.stream()
                    .collect(Collectors.groupingBy(r -> r.getCart().getId()));
            
            // Remove cart items for expired reservations
            for (Map.Entry<UUID, List<CartReservation>> entry : expiredByCart.entrySet()) {
                UUID cartId = entry.getKey();
                List<CartReservation> cartExpiredReservations = entry.getValue();
                
                // Get the cart
                Cart cart = cartRepository.findById(cartId).orElse(null);
                if (cart != null && cart.getItems() != null) {
                    // Remove cart items that match expired reservations
                    List<UUID> expiredProductIds = cartExpiredReservations.stream()
                            .map(r -> r.getProduct().getId())
                            .collect(Collectors.toList());
                    
                    cart.getItems().removeIf(item -> expiredProductIds.contains(item.getProduct().getId()));
                    cartRepository.save(cart);
                    log.info("Removed cart items for expired reservations in cart {}", cartId);
                }
            }
            
            int count = expired.size();
            cartReservationRepository.deleteAll(expired);
            log.info("Cleaned up {} expired cart reservations and removed associated cart items", count);
        }
    }
    
    /**
     * Get available stock for a product (considering cart reservations)
     * Also checks Product.quantity as the base constraint
     */
    public int getAvailableStock(UUID productId) {
        // First check Product.quantity - if it's 0, nothing is available
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return 0;
        }
        
        int productQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
        if (productQuantity <= 0) {
            return 0; // No stock available if product quantity is 0 or less
        }
        
        Inventory inventory = inventoryRepository.findById(productId).orElse(null);
        if (inventory == null) {
            // If no inventory record exists, use product quantity minus cart reservations
            int reservedInCarts = sumReservedQuantityForProduct(productId);
            return Math.max(0, productQuantity - reservedInCarts);
        }
        
        // Use the minimum of product quantity and inventory quantity as the base
        int baseQuantity = Math.min(productQuantity, inventory.getQuantity());
        int reservedInCarts = sumReservedQuantityForProduct(productId);
        return Math.max(0, baseQuantity - inventory.getReserved() - reservedInCarts);
    }
    
    /**
     * Get available stock for a product, excluding a specific cart
     * Used when converting cart to order - excludes user's own cart reservations
     * Also checks Product.quantity as the base constraint
     */
    public int getAvailableStockExcludingCart(UUID productId, UUID excludeCartId) {
        // First check Product.quantity - if it's 0, nothing is available
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return 0;
        }
        
        int productQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
        if (productQuantity <= 0) {
            return 0; // No stock available if product quantity is 0 or less
        }
        
        Inventory inventory = inventoryRepository.findById(productId).orElse(null);
        if (inventory == null) {
            // If no inventory record exists, use product quantity minus cart reservations
            int reservedInCarts = sumReservedQuantityForProductExcludingCart(productId, excludeCartId);
            return Math.max(0, productQuantity - reservedInCarts);
        }
        
        // Use the minimum of product quantity and inventory quantity as the base
        int baseQuantity = Math.min(productQuantity, inventory.getQuantity());
        int reservedInCarts = sumReservedQuantityForProductExcludingCart(productId, excludeCartId);
        return Math.max(0, baseQuantity - inventory.getReserved() - reservedInCarts);
    }
}

