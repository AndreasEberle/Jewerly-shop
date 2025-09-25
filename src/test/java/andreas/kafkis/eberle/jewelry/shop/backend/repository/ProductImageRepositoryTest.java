package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;

@DataJpaTest
@ActiveProfiles("test")
public class ProductImageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;
    private ProductImage primaryImage;
    private ProductImage secondaryImage;

    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = Product.builder()
                .sku("TEST-SKU-001")
                .name("Test Jewelry")
                .description("Test jewelry product")
                .priceCents(10000L)
                .currency("EUR")
                .material("Gold")
                .gemstone("Diamond")
                .weightGrams(BigDecimal.valueOf(10.5))
                .active(true)
                .build();
        
        testProduct = productRepository.save(testProduct);

        // Create primary image
        primaryImage = ProductImage.builder()
                .product(testProduct)
                .storageKey("products/" + testProduct.getId() + "/primary.webp")
                .url("http://localhost:8080/files/products/" + testProduct.getId() + "/primary.webp")
                .isPrimary(true)
                .sortOrder(0)
                .altText("Primary jewelry image")
                .width(800)
                .height(600)
                .mimeType("image/webp")
                .createdAt(OffsetDateTime.now())
                .build();

        // Create secondary image
        secondaryImage = ProductImage.builder()
                .product(testProduct)
                .storageKey("products/" + testProduct.getId() + "/secondary.webp")
                .url("http://localhost:8080/files/products/" + testProduct.getId() + "/secondary.webp")
                .isPrimary(false)
                .sortOrder(1)
                .altText("Secondary jewelry image")
                .width(400)
                .height(300)
                .mimeType("image/webp")
                .createdAt(OffsetDateTime.now())
                .build();

        productImageRepository.save(primaryImage);
        productImageRepository.save(secondaryImage);
    }

    @Test
    void testFindByProductIdOrderByIsPrimaryDescSortOrderAsc() {
        // When
        List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(testProduct.getId());

        // Then
        assertThat(images).hasSize(2);
        assertThat(images.get(0).isPrimary()).isTrue(); // Primary image first
        assertThat(images.get(1).isPrimary()).isFalse(); // Secondary image second
        assertThat(images.get(0).getSortOrder()).isEqualTo(0);
        assertThat(images.get(1).getSortOrder()).isEqualTo(1);
    }

    @Test
    void testFindByProductIdWithNonExistentProduct() {
        // When
        UUID nonExistentId = UUID.randomUUID();
        List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(nonExistentId);

        // Then
        assertThat(images).isEmpty();
    }

    @Test
    void testSaveProductImage() {
        // Given
        ProductImage newImage = ProductImage.builder()
                .product(testProduct)
                .storageKey("products/" + testProduct.getId() + "/new.webp")
                .isPrimary(false)
                .sortOrder(2)
                .altText("New image")
                .width(200)
                .height(150)
                .mimeType("image/webp")
                .createdAt(OffsetDateTime.now())
                .build();

        // When
        ProductImage savedImage = productImageRepository.save(newImage);

        // Then
        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getProduct().getId()).isEqualTo(testProduct.getId());
        assertThat(savedImage.getStorageKey()).isEqualTo("products/" + testProduct.getId() + "/new.webp");
    }

    @Test
    void testDeleteProductImage() {
        // Given
        UUID imageId = primaryImage.getId();

        // When
        productImageRepository.deleteById(imageId);

        // Then
        assertThat(productImageRepository.findById(imageId)).isEmpty();
    }

    @Test
    void testCountByProductId() {
        // When
        long count = productImageRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}
