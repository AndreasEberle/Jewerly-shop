package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    
    Page<Order> findByCustomerOrderByOrderDateDesc(User customer, Pageable pageable);
    
    Page<Order> findByStatusOrderByOrderDateDesc(Order.OrderStatus status, Pageable pageable);
    
    @Query(value = "SELECT COUNT(o.id) FROM orders o WHERE o.status = CAST(:status AS order_status)", nativeQuery = true)
    long countByStatus(@Param("status") String status);
    
    long countByCreatedAtAfter(OffsetDateTime date);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate")
    long countByCreatedAtAfterQuery(@Param("startDate") OffsetDateTime startDate);
}


