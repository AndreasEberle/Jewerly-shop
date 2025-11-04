package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Cart;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CartRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.CartReservationService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Product Availability", description = "Real-time product availability endpoints")
@Slf4j
public class ProductAvailabilityController {

    @Autowired
    private CartReservationService cartReservationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CartRepository cartRepository;

    @GetMapping("/availability")
    @Operation(summary = "Get availability for multiple products")
    public ResponseEntity<Map<UUID, Integer>> getAvailability(
            @RequestParam List<UUID> productIds,
            Authentication authentication) {
        try {
            UUID excludeCartId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    User user = userService.findByEmail(authentication.getName());
                    if (user != null) {
                        List<Cart> userCarts = cartRepository.findByUser(user);
                        if (!userCarts.isEmpty()) {
                            excludeCartId = userCarts.get(0).getId();
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not get user cart for availability exclusion: {}", e.getMessage());
                }
            }
            
            final UUID finalExcludeCartId = excludeCartId;
            Map<UUID, Integer> availability = productIds.stream()
                    .collect(Collectors.toMap(
                            id -> id,
                            id -> finalExcludeCartId != null 
                                ? cartReservationService.getAvailableStockExcludingCart(id, finalExcludeCartId)
                                : cartReservationService.getAvailableStock(id)
                    ));
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            log.error("Error getting product availability: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{productId}/availability")
    @Operation(summary = "Get availability for a single product")
    public ResponseEntity<Map<String, Integer>> getProductAvailability(
            @PathVariable UUID productId,
            Authentication authentication) {
        try {
            UUID excludeCartId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    User user = userService.findByEmail(authentication.getName());
                    if (user != null) {
                        List<Cart> userCarts = cartRepository.findByUser(user);
                        if (!userCarts.isEmpty()) {
                            excludeCartId = userCarts.get(0).getId();
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not get user cart for availability exclusion: {}", e.getMessage());
                }
            }
            
            int available = excludeCartId != null 
                ? cartReservationService.getAvailableStockExcludingCart(productId, excludeCartId)
                : cartReservationService.getAvailableStock(productId);
            Map<String, Integer> response = new HashMap<>();
            response.put("availableQuantity", available);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting product availability: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

