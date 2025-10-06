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
    
    long countByStatus(Payment.PaymentStatus status);
    
    long countByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT AVG(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal avgAmountByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p GROUP BY p.paymentMethod")
    Map<String, Long> countByPaymentMethod();
}


