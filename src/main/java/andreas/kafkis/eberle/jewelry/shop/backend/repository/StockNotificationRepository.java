package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.StockNotification;

@Repository
public interface StockNotificationRepository extends JpaRepository<StockNotification, UUID> {
    
    Optional<StockNotification> findByProductIdAndUserId(UUID productId, UUID userId);
    
    Optional<StockNotification> findByProductIdAndEmail(UUID productId, String email);
    
    List<StockNotification> findByProductIdOrderByCreatedAtDesc(UUID productId);
    
    List<StockNotification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    List<StockNotification> findByNotifiedFalseOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(sn) FROM StockNotification sn WHERE sn.product.id = :productId")
    long countByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(sn) FROM StockNotification sn WHERE sn.product.id = :productId AND sn.notified = false")
    long countByProductIdAndNotNotified(@Param("productId") UUID productId);
    
    @Query("SELECT sn FROM StockNotification sn WHERE sn.createdAt >= :since ORDER BY sn.createdAt DESC")
    List<StockNotification> findByCreatedAtAfter(@Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(sn) FROM StockNotification sn WHERE sn.createdAt >= :since")
    long countByCreatedAtAfter(@Param("since") OffsetDateTime since);
}

