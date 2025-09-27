package andreas.kafkis.eberle.jewelry.shop.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Category;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Tag;
import andreas.kafkis.eberle.jewelry.shop.backend.exception.ResourceNotFoundException;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.InventoryRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Rings");
        testCategory.setSlug("rings");

        testTag = new Tag();
        testTag.setId(1);
        testTag.setName("Gold");
        testTag.setSlug("gold");

        testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setName("Diamond Ring");
        testProduct.setSku("RING001");
        testProduct.setDescription("Beautiful diamond ring");
        testProduct.setPriceCents(500000L); // $5000.00
        testProduct.setCurrency("USD");
        testProduct.setMaterial("Gold");
        testProduct.setGemstone("Diamond");
        testProduct.setWeightGrams(BigDecimal.valueOf(5.5));
        testProduct.setActive(true);
        testProduct.setCreatedAt(OffsetDateTime.now());
        testProduct.setUpdatedAt(OffsetDateTime.now());
        testProduct.setCategories(java.util.Set.of(testCategory));
        testProduct.setTags(java.util.Set.of(testTag));
    }

    @Test
    void listAll_ShouldReturnAllProducts() {
        // Given
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        // When
        List<Product> products = productService.listAll();

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0)).isEqualTo(testProduct);
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        UUID productId = testProduct.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Product product = productService.get(productId);

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(productId);
        assertThat(product.getName()).isEqualTo("Diamond Ring");
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldThrowException() {
        // Given
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.get(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with ID: " + productId);
    }

    @Test
    void create_WhenValidProduct_ShouldCreateProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product createdProduct = productService.create(testProduct);

        // Then
        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getName()).isEqualTo("Diamond Ring");
        verify(productRepository).save(testProduct);
    }

    @Test
    void findActiveProducts_ShouldReturnOnlyActiveProducts() {
        // Given
        when(productRepository.findByActiveTrue()).thenReturn(List.of(testProduct));

        // When
        List<Product> products = productService.findActiveProducts();

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).isActive()).isTrue();
    }

    @Test
    void findByCategory_WhenCategoryExists_ShouldReturnProducts() {
        // Given
        when(productRepository.findByCategoriesName("Rings")).thenReturn(List.of(testProduct));

        // When
        List<Product> products = productService.findByCategory("Rings");

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getCategories()).contains(testCategory);
    }

    @Test
    void findAllWithPagination_ShouldReturnPaginatedProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // When
        Page<Product> result = productService.findAllWithPagination(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void searchProducts_WithSearchTerm_ShouldReturnFilteredProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        // When
        Page<Product> result = productService.searchProducts("diamond", null, null, null, null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchProducts_WithCategoryFilter_ShouldReturnFilteredProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        // When
        Page<Product> result = productService.searchProducts(null, "Rings", null, null, null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchProducts_WithPriceRange_ShouldReturnFilteredProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        // When
        Page<Product> result = productService.searchProducts(null, null, null, null, BigDecimal.valueOf(1000), BigDecimal.valueOf(10000), null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchProducts_WithActiveFilter_ShouldReturnFilteredProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        // When
        Page<Product> result = productService.searchProducts(null, null, null, null, null, null, true, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
