package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.UserFavorite;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserFavoriteRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;

@Service
@Transactional
public class UserFavoriteService {
    
    @Autowired
    private UserFavoriteRepository userFavoriteRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;
    
    /**
     * Add a product to user's favorites
     */
    public void addToFavorites(UUID userId, UUID productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if already in favorites
        if (userFavoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return; // Already in favorites
        }
        
        UserFavorite favorite = UserFavorite.builder()
                .user(user)
                .product(product)
                .build();
        
        userFavoriteRepository.save(favorite);
    }
    
    /**
     * Remove a product from user's favorites
     */
    public void removeFromFavorites(UUID userId, UUID productId) {
        userFavoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    /**
     * Toggle favorite status
     */
    public boolean toggleFavorite(UUID userId, UUID productId) {
        if (userFavoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            removeFromFavorites(userId, productId);
            return false; // Removed
        } else {
            addToFavorites(userId, productId);
            return true; // Added
        }
    }
    
    /**
     * Get user's favorite products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getUserFavorites(UUID userId) {
        List<UserFavorite> favorites = userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(favorite -> productService.convertToDTO(favorite.getProduct()))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if product is in user's favorites
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(UUID userId, UUID productId) {
        return userFavoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
    
    /**
     * Get user's favorite product IDs
     */
    @Transactional(readOnly = true)
    public List<UUID> getUserFavoriteProductIds(UUID userId) {
        return userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(favorite -> favorite.getProduct().getId())
                .collect(Collectors.toList());
    }
    
    /**
     * Get favorite count for a product
     */
    @Transactional(readOnly = true)
    public long getFavoriteCount(UUID productId) {
        return userFavoriteRepository.countByProductId(productId);
    }
}



