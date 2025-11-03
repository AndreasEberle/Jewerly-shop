package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductReview;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    
    List<ProductReview> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(UUID productId);
    
    Page<ProductReview> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(UUID productId, Pageable pageable);
    
    Optional<ProductReview> findByUserIdAndProductId(UUID userId, UUID productId);
    
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
    
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    Long countByProductIdAndIsApprovedTrue(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.rating = :rating AND r.isApproved = true")
    Long countByProductIdAndRatingAndIsApprovedTrue(@Param("productId") UUID productId, @Param("rating") Integer rating);
    
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.isVerifiedPurchase = true AND r.isApproved = true")
    Long countByProductIdAndIsVerifiedPurchaseTrueAndIsApprovedTrue(@Param("productId") UUID productId);
    
    List<ProductReview> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Page<ProductReview> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}



