package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.DiscountCode;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, UUID> {
    
    Optional<DiscountCode> findByCodeIgnoreCase(String code);
    
    List<DiscountCode> findByIsActiveTrue();
    
    @Query("SELECT dc FROM DiscountCode dc WHERE dc.isActive = true " +
           "AND (dc.validFrom IS NULL OR dc.validFrom <= :now) " +
           "AND (dc.validUntil IS NULL OR dc.validUntil >= :now) " +
           "AND (dc.usageLimit IS NULL OR dc.usageCount < dc.usageLimit)")
    List<DiscountCode> findValidAndActiveCodes(@Param("now") OffsetDateTime now);
    
    @Query("SELECT COUNT(du) FROM DiscountCodeUsage du WHERE du.discountCode.id = :discountCodeId AND du.user.id = :userId")
    Long countUsagesByUser(@Param("discountCodeId") UUID discountCodeId, @Param("userId") UUID userId);
}


