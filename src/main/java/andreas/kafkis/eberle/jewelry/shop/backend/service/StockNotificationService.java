package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.StockNotification;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.StockNotificationRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockNotificationService {
    
    @Autowired
    private StockNotificationRepository stockNotificationRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Request notification when product becomes available
     */
    @Transactional
    public StockNotification requestNotification(UUID productId, String email, UUID userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        // Check if notification already exists
        Optional<StockNotification> existing;
        if (userId != null) {
            existing = stockNotificationRepository.findByProductIdAndUserId(productId, userId);
        } else {
            existing = stockNotificationRepository.findByProductIdAndEmail(productId, email);
        }
        
        if (existing.isPresent()) {
            throw new RuntimeException("You have already requested to be notified for this product");
        }
        
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        
        StockNotification notification = StockNotification.builder()
                .product(product)
                .user(user)
                .email(email)
                .notified(false)
                .build();
        
        StockNotification saved = stockNotificationRepository.save(notification);
        log.info("Stock notification requested: productId={}, email={}, userId={}", productId, email, userId);
        
        return saved;
    }
    
    /**
     * Get all notifications (admin)
     */
    @Transactional(readOnly = true)
    public List<StockNotification> getAllNotifications() {
        return stockNotificationRepository.findAll();
    }
    
    /**
     * Get notifications by product
     */
    @Transactional(readOnly = true)
    public List<StockNotification> getNotificationsByProduct(UUID productId) {
        return stockNotificationRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }
    
    /**
     * Get notifications created after a specific date
     */
    @Transactional(readOnly = true)
    public List<StockNotification> getNotificationsSince(OffsetDateTime since) {
        return stockNotificationRepository.findByCreatedAtAfter(since);
    }
    
    /**
     * Get count of notifications for a product
     */
    @Transactional(readOnly = true)
    public long getNotificationCount(UUID productId) {
        return stockNotificationRepository.countByProductId(productId);
    }
    
    /**
     * Get count of pending (not notified) requests for a product
     */
    @Transactional(readOnly = true)
    public long getPendingNotificationCount(UUID productId) {
        return stockNotificationRepository.countByProductIdAndNotNotified(productId);
    }
    
    /**
     * Get count of notifications created after a specific date
     */
    @Transactional(readOnly = true)
    public long getNotificationCountSince(OffsetDateTime since) {
        return stockNotificationRepository.countByCreatedAtAfter(since);
    }
    
    /**
     * Mark notification as notified
     */
    @Transactional
    public void markAsNotified(UUID notificationId) {
        StockNotification notification = stockNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        notification.setNotified(true);
        notification.setNotifiedAt(OffsetDateTime.now());
        stockNotificationRepository.save(notification);
    }
    
    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(UUID notificationId) {
        stockNotificationRepository.deleteById(notificationId);
    }
}

