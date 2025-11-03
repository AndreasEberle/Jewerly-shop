package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserFavoriteService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;

@RestController
@RequestMapping("/api/user/favorites")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class UserFavoriteController {
    
    @Autowired
    private UserFavoriteService userFavoriteService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Helper method to get current user from authentication
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return user;
    }
    
    /**
     * Get user's favorite products
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getUserFavorites(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<ProductDTO> favorites = userFavoriteService.getUserFavorites(user.getId());
        return ResponseEntity.ok(favorites);
    }
    
    /**
     * Add product to favorites
     */
    @PostMapping("/{productId}")
    public ResponseEntity<String> addToFavorites(@PathVariable UUID productId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        userFavoriteService.addToFavorites(user.getId(), productId);
        return ResponseEntity.ok("Product added to favorites");
    }
    
    /**
     * Remove product from favorites
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromFavorites(@PathVariable UUID productId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        userFavoriteService.removeFromFavorites(user.getId(), productId);
        return ResponseEntity.ok("Product removed from favorites");
    }
    
    /**
     * Toggle favorite status
     */
    @PostMapping("/{productId}/toggle")
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable UUID productId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        boolean isFavorite = userFavoriteService.toggleFavorite(user.getId(), productId);
        return ResponseEntity.ok(isFavorite);
    }
    
    /**
     * Check if product is in favorites
     */
    @GetMapping("/{productId}/status")
    public ResponseEntity<Boolean> isFavorite(@PathVariable UUID productId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        boolean isFavorite = userFavoriteService.isFavorite(user.getId(), productId);
        return ResponseEntity.ok(isFavorite);
    }
    
    /**
     * Get user's favorite product IDs
     */
    @GetMapping("/ids")
    public ResponseEntity<List<UUID>> getUserFavoriteIds(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        List<UUID> favoriteIds = userFavoriteService.getUserFavoriteProductIds(user.getId());
        return ResponseEntity.ok(favoriteIds);
    }
}
