package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemMetrics;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, UUID> {
    
    Optional<SystemMetrics> findFirstByMetricNameOrderByCreatedAtDesc(String metricName);
    
    @Query("SELECT sm FROM SystemMetrics sm " +
           "WHERE sm.metricName = :metricName " +
           "AND sm.createdAt >= :since " +
           "ORDER BY sm.createdAt ASC")
    List<SystemMetrics> findMetricsByNameSince(@Param("metricName") String metricName, @Param("since") OffsetDateTime since);
    
    @Query("SELECT sm.metricName, AVG(sm.metricValue) as avgValue " +
           "FROM SystemMetrics sm " +
           "WHERE sm.createdAt >= :since " +
           "GROUP BY sm.metricName")
    List<Object[]> findAverageMetricsSince(@Param("since") OffsetDateTime since);
}


