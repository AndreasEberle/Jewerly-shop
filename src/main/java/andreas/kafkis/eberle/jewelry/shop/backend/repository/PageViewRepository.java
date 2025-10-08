package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.PageView;

@Repository
public interface PageViewRepository extends JpaRepository<PageView, UUID> {
    
    @Query("SELECT COUNT(pv) FROM PageView pv WHERE pv.createdAt >= :since")
    Long countPageViewsSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT pv.countryCode, COUNT(pv) as viewCount " +
           "FROM PageView pv " +
           "WHERE pv.createdAt >= :since AND pv.countryCode IS NOT NULL " +
           "GROUP BY pv.countryCode " +
           "ORDER BY viewCount DESC")
    List<Object[]> findPageViewsByCountrySince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT pv.pagePath, COUNT(pv) as viewCount " +
           "FROM PageView pv " +
           "WHERE pv.createdAt >= :since " +
           "GROUP BY pv.pagePath " +
           "ORDER BY viewCount DESC")
    List<Object[]> findTopPagesSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT DATE(pv.createdAt) as date, COUNT(pv) as viewCount " +
           "FROM PageView pv " +
           "WHERE pv.createdAt >= :since " +
           "GROUP BY DATE(pv.createdAt) " +
           "ORDER BY date ASC")
    List<Object[]> findDailyPageViewsSince(@Param("since") OffsetDateTime since);
}


