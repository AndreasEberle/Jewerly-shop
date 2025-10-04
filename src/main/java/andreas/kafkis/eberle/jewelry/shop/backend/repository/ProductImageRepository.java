package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
    List<ProductImage> findByProductIdOrderByIsPrimaryDescSortOrderAsc(UUID productId);
    List<ProductImage> findByProductOrderBySortOrder(Product product);
    Optional<ProductImage> findByProductAndIsPrimaryTrue(Product product);
}


