package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    
    /**
     * Find products with low stock (available stock below threshold)
     */
    @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reserved) <= :threshold")
    List<Inventory> findLowStockProducts(@Param("threshold") int threshold);
    
    /**
     * Find products that are out of stock
     */
    @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reserved) <= 0")
    List<Inventory> findOutOfStockProducts();
    
    /**
     * Find products with available stock
     */
    @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reserved) > 0")
    List<Inventory> findInStockProducts();
}


