package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.AnalyticsEvent;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {
    
    // Count events by type
    long countByEventType(AnalyticsEvent.EventType eventType);
    
    // Count events by type and date range
    long countByEventTypeAndCreatedAtBetween(
        AnalyticsEvent.EventType eventType, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    // Get most viewed products
    @Query("SELECT ae.entityId, COUNT(ae) as viewCount " +
           "FROM AnalyticsEvent ae " +
           "WHERE ae.eventType = 'PRODUCT_VIEW' " +
           "AND ae.entityId IS NOT NULL " +
           "GROUP BY ae.entityId " +
           "ORDER BY viewCount DESC")
    List<Object[]> findMostViewedProducts();
    
    // Get most viewed products with limit (using Pageable)
    @Query("SELECT ae.entityId, COUNT(ae) as viewCount " +
           "FROM AnalyticsEvent ae " +
           "WHERE ae.eventType = 'PRODUCT_VIEW' " +
           "AND ae.entityId IS NOT NULL " +
           "GROUP BY ae.entityId " +
           "ORDER BY COUNT(ae) DESC")
    List<Object[]> findMostViewedProducts(org.springframework.data.domain.Pageable pageable);
    
    // Get events by date range
    List<AnalyticsEvent> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Count events by date range
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Get events by type and date range
    List<AnalyticsEvent> findByEventTypeAndCreatedAtBetween(
        AnalyticsEvent.EventType eventType, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    // Get daily event counts (PostgreSQL compatible)
    @Query("SELECT CAST(ae.createdAt AS date) as eventDate, ae.eventType, COUNT(ae) as eventCount " +
           "FROM AnalyticsEvent ae " +
           "WHERE ae.createdAt >= :startDate " +
           "GROUP BY CAST(ae.createdAt AS date), ae.eventType " +
           "ORDER BY eventDate DESC")
    List<Object[]> findDailyEventCounts(@Param("startDate") LocalDateTime startDate);
    
    // Get hourly event counts for today (PostgreSQL compatible)
    @Query("SELECT EXTRACT(HOUR FROM ae.createdAt) as eventHour, ae.eventType, COUNT(ae) as eventCount " +
           "FROM AnalyticsEvent ae " +
           "WHERE ae.createdAt >= :startOfDay AND ae.createdAt < :endOfDay " +
           "GROUP BY EXTRACT(HOUR FROM ae.createdAt), ae.eventType " +
           "ORDER BY eventHour")
    List<Object[]> findHourlyEventCounts(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // Get recent search queries with limit (using Pageable)
    @Query("SELECT ae.additionalData " +
           "FROM AnalyticsEvent ae " +
           "WHERE ae.eventType = 'PRODUCT_SEARCH' " +
           "AND ae.additionalData IS NOT NULL " +
           "ORDER BY ae.createdAt DESC")
    List<String> findRecentSearchQueries(org.springframework.data.domain.Pageable pageable);
}
