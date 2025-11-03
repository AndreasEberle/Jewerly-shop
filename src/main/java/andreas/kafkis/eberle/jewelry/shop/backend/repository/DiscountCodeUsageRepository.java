package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.DiscountCodeUsage;

@Repository
public interface DiscountCodeUsageRepository extends JpaRepository<DiscountCodeUsage, UUID> {
    
    List<DiscountCodeUsage> findByDiscountCodeId(UUID discountCodeId);
    
    List<DiscountCodeUsage> findByUserId(UUID userId);
    
    List<DiscountCodeUsage> findByOrderId(UUID orderId);
    
    boolean existsByDiscountCodeIdAndUserId(UUID discountCodeId, UUID userId);
}


