package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Category;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Tag;
import andreas.kafkis.eberle.jewelry.shop.backend.service.JwtService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private SystemConfigService systemConfigService;

    @MockBean
    private JwtService jwtService;

    private Product testProduct;
    private Category testCategory;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        
        testCategory.setName("Rings");
        testCategory.setSlug("rings");

        testTag = new Tag();
        
        testTag.setName("Gold");
        testTag.setSlug("gold");

        testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setName("Diamond Ring");
        testProduct.setSku("RING001");
        testProduct.setDescription("Beautiful diamond ring");
        testProduct.setPriceCents(500000L); // $5000.00
        testProduct.setBaseCurrency("USD");
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
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        // Given
        when(productService.listAll()).thenReturn(List.of(testProduct));

        // When & Then
        mockMvc.perform(get("/api/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Diamond Ring"))
                .andExpect(jsonPath("$[0].sku").value("RING001"));
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
        // Given
        UUID productId = testProduct.getId();
        when(productService.get(productId)).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Diamond Ring"))
                .andExpect(jsonPath("$.sku").value("RING001"));
    }

    @Test
    void getActiveProducts_ShouldReturnOnlyActiveProducts() throws Exception {
        // Given
        when(productService.findActiveProducts()).thenReturn(List.of(testProduct));

        // When & Then
        mockMvc.perform(get("/api/products/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @WithMockUser
    void getProductsByCategory_ShouldReturnProductsInCategory() throws Exception {
        // Given
        when(productService.findByCategory("Rings")).thenReturn(List.of(testProduct));

        // When & Then
        mockMvc.perform(get("/api/products/category/Rings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Diamond Ring"));
    }

    @Test
    void searchProducts_WithSearchTerm_ShouldReturnFilteredProducts() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productService.searchProducts(anyString(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/products")
                .param("search", "diamond")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Diamond Ring"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchProducts_WithCategoryFilter_ShouldReturnFilteredProducts() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productService.searchProducts(any(), anyString(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/products")
                .param("category", "Rings")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Diamond Ring"));
    }

    @Test
    void searchProducts_WithPriceRange_ShouldReturnFilteredProducts() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productService.searchProducts(any(), any(), any(), any(), any(BigDecimal.class), any(BigDecimal.class), any(), any(Pageable.class)))
                .thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/products")
                .param("minPrice", "1000")
                .param("maxPrice", "10000")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Diamond Ring"));
    }
}
