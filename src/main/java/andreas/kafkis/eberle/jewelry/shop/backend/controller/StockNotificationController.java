package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.StockNotification;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.StockNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Stock Notifications", description = "Stock notification management")
@Slf4j
public class StockNotificationController {
    
    @Autowired
    private StockNotificationService stockNotificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Request notification when product becomes available (public endpoint)
     */
    @PostMapping("/public/stock-notifications/request")
    @Operation(summary = "Request to be notified when a product becomes available")
    public ResponseEntity<Map<String, Object>> requestNotification(
            @RequestParam UUID productId,
            @RequestParam String email,
            Authentication authentication) {
        try {
            UUID userId = null;
            if (authentication != null) {
                String userEmail = authentication.getName();
                var user = userRepository.findByEmail(userEmail);
                if (user != null) {
                    userId = user.getId();
                }
            }
            
            StockNotification notification = stockNotificationService.requestNotification(productId, email, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "You will be notified when this product becomes available");
            response.put("notificationId", notification.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error requesting stock notification: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get all stock notifications (admin only)
     */
    @GetMapping("/admin/stock-notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all stock notifications")
    public ResponseEntity<List<StockNotification>> getAllNotifications(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since) {
        try {
            List<StockNotification> notifications;
            if (since != null) {
                notifications = stockNotificationService.getNotificationsSince(since);
            } else {
                notifications = stockNotificationService.getAllNotifications();
            }
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting stock notifications: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get notifications for a specific product (admin only)
     */
    @GetMapping("/admin/stock-notifications/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notifications for a specific product")
    public ResponseEntity<List<StockNotification>> getNotificationsByProduct(@PathVariable UUID productId) {
        try {
            List<StockNotification> notifications = stockNotificationService.getNotificationsByProduct(productId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting stock notifications for product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get notification statistics (admin only)
     */
    @GetMapping("/admin/stock-notifications/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get stock notification statistics")
    public ResponseEntity<Map<String, Object>> getNotificationStats(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            if (productId != null) {
                stats.put("totalRequests", stockNotificationService.getNotificationCount(productId));
                stats.put("pendingRequests", stockNotificationService.getPendingNotificationCount(productId));
            }
            
            if (since != null) {
                stats.put("requestsSince", stockNotificationService.getNotificationCountSince(since));
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting notification stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Mark notification as notified (admin only)
     */
    @PutMapping("/admin/stock-notifications/{notificationId}/notify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark notification as notified")
    public ResponseEntity<Map<String, Object>> markAsNotified(@PathVariable UUID notificationId) {
        try {
            stockNotificationService.markAsNotified(notificationId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marked as notified");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error marking notification as notified: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete notification (admin only)
     */
    @DeleteMapping("/admin/stock-notifications/{notificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a stock notification")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable UUID notificationId) {
        try {
            stockNotificationService.deleteNotification(notificationId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting notification: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

