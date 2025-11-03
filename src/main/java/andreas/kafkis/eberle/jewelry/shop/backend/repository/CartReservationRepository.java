package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.CartReservation;

@Repository
public interface CartReservationRepository extends JpaRepository<CartReservation, UUID> {
    
    Optional<CartReservation> findByCartIdAndProductId(UUID cartId, UUID productId);
    
    List<CartReservation> findByCartId(UUID cartId);
    
    List<CartReservation> findByProductId(UUID productId);
    
    @Query("SELECT r FROM CartReservation r WHERE r.expiresAt < :now")
    List<CartReservation> findExpiredReservations(@Param("now") OffsetDateTime now);
    
    @Modifying
    @Query("DELETE FROM CartReservation r WHERE r.expiresAt < :now")
    void deleteExpiredReservations(@Param("now") OffsetDateTime now);
    
    @Query("SELECT SUM(r.quantity) FROM CartReservation r WHERE r.product.id = :productId AND r.expiresAt > :now")
    Integer sumReservedQuantityForProduct(@Param("productId") UUID productId, @Param("now") OffsetDateTime now);
    
    @Query("SELECT SUM(r.quantity) FROM CartReservation r WHERE r.product.id = :productId AND r.expiresAt > :now AND (r.cart.id != :excludeCartId OR :excludeCartId IS NULL)")
    Integer sumReservedQuantityForProductExcludingCart(@Param("productId") UUID productId, @Param("now") OffsetDateTime now, @Param("excludeCartId") UUID excludeCartId);
}

