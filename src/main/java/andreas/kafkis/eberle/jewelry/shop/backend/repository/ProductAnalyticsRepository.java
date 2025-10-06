package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductAnalytics;

@Repository
public interface ProductAnalyticsRepository extends JpaRepository<ProductAnalytics, UUID> {
    
    @Query("SELECT pa.product.id, pa.product.name, COUNT(pa) as viewCount " +
           "FROM ProductAnalytics pa " +
           "WHERE pa.eventType = 'view' AND pa.createdAt >= :since " +
           "GROUP BY pa.product.id, pa.product.name " +
           "ORDER BY viewCount DESC")
    List<Object[]> findTopProductsByViewsSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT pa.product.id, pa.product.name, COUNT(pa) as orderCount " +
           "FROM ProductAnalytics pa " +
           "WHERE pa.eventType = 'cart_add' AND pa.createdAt >= :since " +
           "GROUP BY pa.product.id, pa.product.name " +
           "ORDER BY orderCount DESC")
    List<Object[]> findTopProductsByOrdersSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(pa) FROM ProductAnalytics pa WHERE pa.eventType = :eventType AND pa.createdAt >= :since")
    Long countProductEventsByTypeSince(@Param("eventType") String eventType, @Param("since") OffsetDateTime since);
    
    @Query("SELECT pa.countryCode, COUNT(pa) as viewCount " +
           "FROM ProductAnalytics pa " +
           "WHERE pa.eventType = 'view' AND pa.createdAt >= :since AND pa.countryCode IS NOT NULL " +
           "GROUP BY pa.countryCode " +
           "ORDER BY viewCount DESC")
    List<Object[]> findProductViewsByCountrySince(@Param("since") OffsetDateTime since);
}
