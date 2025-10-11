package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.UserFavorite;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UUID> {
    
    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Optional<UserFavorite> findByUserIdAndProductId(UUID userId, UUID productId);
    
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
    
    void deleteByUserIdAndProductId(UUID userId, UUID productId);
    
    @Query("SELECT COUNT(f) FROM UserFavorite f WHERE f.product.id = :productId")
    long countByProductId(@Param("productId") UUID productId);
}
