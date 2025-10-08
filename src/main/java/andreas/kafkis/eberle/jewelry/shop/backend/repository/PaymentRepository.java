package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
    
    @Query(value = "SELECT COUNT(p.id) FROM payments p WHERE p.status = CAST(:status AS payment_status)", nativeQuery = true)
    long countByStatus(@Param("status") String status);
    
    long countByCreatedAtAfter(LocalDateTime date);
    
    @Query(value = "SELECT SUM(p.amount) FROM payments p WHERE p.status = CAST(:status AS payment_status)", nativeQuery = true)
    BigDecimal sumAmountByStatus(@Param("status") String status);
    
    @Query(value = "SELECT AVG(p.amount) FROM payments p WHERE p.status = CAST(:status AS payment_status)", nativeQuery = true)
    BigDecimal avgAmountByStatus(@Param("status") String status);
    
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p GROUP BY p.paymentMethod")
    Map<String, Long> countByPaymentMethod();
}


