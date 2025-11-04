package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Cart;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.CartItem;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CartItemRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CartRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart operations")
@Slf4j
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.service.CartReservationService cartReservationService;

    @GetMapping
    @Operation(summary = "Get user's cart")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Cart cart = getOrCreateCart(user);
            CartResponse response = buildCartResponse(cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting cart for user", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartResponse> addItem(@RequestBody AddItemRequest request, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Cart cart = getOrCreateCart(user);
            
            // Reserve stock for the cart item
            try {
                cartReservationService.reserveStockForCart(cart, product, request.getQuantity());
            } catch (RuntimeException e) {
                log.warn("Failed to reserve stock: {}", e.getMessage());
                return ResponseEntity.badRequest().body(null); // Return bad request if stock unavailable
            }
            
            // Check if item already exists in cart
            CartItem existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                // Release old reservation and create new one with updated quantity
                cartReservationService.releaseReservation(cart.getId(), product.getId());
                cartReservationService.reserveStockForCart(cart, product, existingItem.getQuantity() + request.getQuantity());
                existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            } else {
                // Add new item
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(request.getQuantity())
                        .unitPriceCents(product.getPriceCents())
                        .build();
                cart.getItems().add(newItem);
            }

            cartRepository.save(cart);
            CartResponse response = buildCartResponse(cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding item to cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<CartResponse> updateItem(@PathVariable UUID itemId, @RequestBody UpdateItemRequest request, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            CartItem item = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            // Verify the item belongs to the user's cart
            if (!item.getCart().getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().build();
            }

            if (request.getQuantity() <= 0) {
                // Release reservation before deleting
                cartReservationService.releaseReservation(item.getCart().getId(), item.getProduct().getId());
                cartItemRepository.delete(item);
            } else {
                // Update reservation with new quantity (this will also extend all cart reservations)
                cartReservationService.releaseReservation(item.getCart().getId(), item.getProduct().getId());
                try {
                    cartReservationService.reserveStockForCart(item.getCart(), item.getProduct(), request.getQuantity());
                    item.setQuantity(request.getQuantity());
                    cartItemRepository.save(item);
                } catch (RuntimeException e) {
                    log.warn("Failed to reserve stock: {}", e.getMessage());
                    return ResponseEntity.badRequest().body(null);
                }
            }

            Cart cart = getOrCreateCart(user);
            CartResponse response = buildCartResponse(cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating cart item", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeItem(@PathVariable UUID itemId, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            CartItem item = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            // Verify the item belongs to the user's cart
            if (!item.getCart().getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().build();
            }

            // Release reservation before deleting
            cartReservationService.releaseReservation(item.getCart().getId(), item.getProduct().getId());
            cartItemRepository.delete(item);

            Cart cart = getOrCreateCart(user);
            CartResponse response = buildCartResponse(cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error removing cart item", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Cart cart = getOrCreateCart(user);
            // Release all reservations
            cartReservationService.releaseAllReservationsForCart(cart.getId());
            cart.getItems().clear();
            cartRepository.save(cart);

            CartResponse response = buildCartResponse(cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper methods
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userService.findByEmail(email);
    }

    @Transactional
    private Cart getOrCreateCart(User user) {
        // First, try to find existing carts
        List<Cart> userCarts = cartRepository.findByUser(user);
        
        if (!userCarts.isEmpty()) {
            if (userCarts.size() == 1) {
                // One cart exists, return it
                return userCarts.get(0);
            } else {
                // Multiple carts exist, merge them and keep the first one
                log.warn("User {} has {} carts, merging them", user.getEmail(), userCarts.size());
                Cart primaryCart = userCarts.get(0);
                
                // Merge all items from other carts into the first cart
                for (int i = 1; i < userCarts.size(); i++) {
                    Cart cartToMerge = userCarts.get(i);
                    for (CartItem item : cartToMerge.getItems()) {
                        // Check if item already exists in primary cart
                        CartItem existingItem = primaryCart.getItems().stream()
                                .filter(existing -> existing.getProduct().getId().equals(item.getProduct().getId()))
                                .findFirst()
                                .orElse(null);
                        
                        if (existingItem != null) {
                            // Update quantity
                            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                        } else {
                            // Add new item
                            item.setCart(primaryCart);
                            primaryCart.getItems().add(item);
                        }
                    }
                    // Delete the merged cart
                    cartRepository.delete(cartToMerge);
                }
                
                return cartRepository.save(primaryCart);
            }
        }
        
        // No cart exists, create a new one
        Cart newCart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();
        try {
            return cartRepository.save(newCart);
        } catch (Exception e) {
            // If save fails due to unique constraint, another thread might have created a cart
            log.warn("Failed to create new cart for user {}, trying to find existing cart", user.getEmail());
            userCarts = cartRepository.findByUser(user);
            if (!userCarts.isEmpty()) {
                return userCarts.get(0);
            }
            throw e;
        }
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::buildCartItemResponse)
                .toList();

        int itemCount = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal total = cart.getItems().stream()
                .map(item -> BigDecimal.valueOf(item.getUnitPriceCents())
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .divide(BigDecimal.valueOf(100)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .itemCount(itemCount)
                .total(total.doubleValue())
                .build();
    }

    private CartItemResponse buildCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productPrice(item.getProduct().getPriceCents() / 100.0)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPriceCents() / 100.0)
                .subtotal((item.getUnitPriceCents() * item.getQuantity()) / 100.0)
                .build();
    }

    @Data
    @lombok.Builder
    public static class CartResponse {
        private UUID id;
        private List<CartItemResponse> items;
        private int itemCount;
        private double total;
    }

    @Data
    @lombok.Builder
    public static class CartItemResponse {
        private UUID id;
        private UUID productId;
        private String productName;
        private double productPrice;
        private int quantity;
        private double unitPrice;
        private double subtotal;
    }

    @Data
    public static class AddItemRequest {
        private UUID productId;
        private Integer quantity;
    }

    @Data
    public static class UpdateItemRequest {
        private Integer quantity;
    }
}
