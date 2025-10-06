package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.UserAnalytics;

@Repository
public interface UserAnalyticsRepository extends JpaRepository<UserAnalytics, UUID> {
    
    @Query("SELECT COUNT(DISTINCT ua.user.id) FROM UserAnalytics ua WHERE ua.createdAt >= :since")
    Long countUniqueUsersSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT ua.countryCode, COUNT(DISTINCT ua.user.id) as userCount " +
           "FROM UserAnalytics ua " +
           "WHERE ua.createdAt >= :since AND ua.countryCode IS NOT NULL " +
           "GROUP BY ua.countryCode " +
           "ORDER BY userCount DESC")
    List<Object[]> findUserCountByCountrySince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT ua FROM UserAnalytics ua " +
           "WHERE ua.eventType = 'registration' " +
           "ORDER BY ua.createdAt DESC")
    List<UserAnalytics> findRecentRegistrations();
    
    @Query("SELECT COUNT(ua) FROM UserAnalytics ua WHERE ua.eventType = :eventType AND ua.createdAt >= :since")
    Long countEventsByTypeSince(@Param("eventType") String eventType, @Param("since") OffsetDateTime since);
}
