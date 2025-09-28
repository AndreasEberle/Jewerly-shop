package andreas.kafkis.eberle.jewelry.shop.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DataDrivenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDataLoadedCorrectly() {
        // Test that our test data was loaded
        assertThat(productRepository.count()).isEqualTo(4);
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(productImageRepository.count()).isEqualTo(0); // No images in simple test data
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testProductWithImages() {
        // Test the diamond ring product with its images
        UUID diamondRingId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        
        Product diamondRing = productRepository.findById(diamondRingId).orElse(null);
        assertThat(diamondRing).isNotNull();
        assertThat(diamondRing.getName()).isEqualTo("Diamond Engagement Ring");
        assertThat(diamondRing.getSku()).isEqualTo("RING001");
        assertThat(diamondRing.getPriceCents()).isEqualTo(500000L);
        assertThat(diamondRing.isActive()).isTrue();

        // Test product images (currently no images in simple test data)
        List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(diamondRingId);
        assertThat(images).isEmpty(); // No images in simple test data
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testProductCategoriesAndTags() {
        UUID diamondRingId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Product diamondRing = productRepository.findById(diamondRingId).orElse(null);
        
        assertThat(diamondRing).isNotNull();
        assertThat(diamondRing.getCategories()).hasSize(1);
        assertThat(diamondRing.getCategories().iterator().next().getName()).isEqualTo("Rings");
        
        assertThat(diamondRing.getTags()).hasSize(2);
        assertThat(diamondRing.getTags()).anyMatch(tag -> tag.getName().equals("Gold"));
        assertThat(diamondRing.getTags()).anyMatch(tag -> tag.getName().equals("Diamond"));
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testInactiveProduct() {
        UUID rubyRingId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
        Product rubyRing = productRepository.findById(rubyRingId).orElse(null);
        
        assertThat(rubyRing).isNotNull();
        assertThat(rubyRing.getName()).isEqualTo("Vintage Ruby Ring");
        assertThat(rubyRing.isActive()).isFalse();
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testUserRoles() {
        UUID adminUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User adminUser = userRepository.findById(adminUserId).orElse(null);
        
        assertThat(adminUser).isNotNull();
        assertThat(adminUser.getEmail()).isEqualTo("admin@jewelry.com");
        assertThat(adminUser.getFirstName()).isEqualTo("Admin");
        assertThat(adminUser.getLastName()).isEqualTo("User");
        assertThat(adminUser.isActive()).isTrue();
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testProductImagesApi() throws Exception {
        UUID diamondRingId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        
        // Test getting all images for a product (no images in simple test data)
        mockMvc.perform(get("/api/products/{productId}/images", diamondRingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Test getting primary image (should return 404 since no images exist)
        mockMvc.perform(get("/api/products/{productId}/images/primary", diamondRingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testProductNotFound() throws Exception {
        UUID nonExistentId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        
        mockMvc.perform(get("/api/products/{productId}/images", nonExistentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/products/{productId}/images/primary", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testProductWithoutImages() throws Exception {
        // The ruby ring has no images in our test data
        UUID rubyRingId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        
        mockMvc.perform(get("/api/products/{productId}/images", rubyRingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/products/{productId}/images/primary", rubyRingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testProductSearchBySku() {
        Product ringBySku = productRepository.findBySku("RING001").orElse(null);
        assertThat(ringBySku).isNotNull();
        assertThat(ringBySku.getName()).isEqualTo("Diamond Engagement Ring");
        
        Product nonExistentSku = productRepository.findBySku("NONEXISTENT").orElse(null);
        assertThat(nonExistentSku).isNull();
    }

    @Test
    @Sql(scripts = {"/test-data-simple.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testUserEmailLookup() {
        User userByEmail = userRepository.findByEmail("customer@jewelry.com").orElse(null);
        assertThat(userByEmail).isNotNull();
        assertThat(userByEmail.getFirstName()).isEqualTo("John");
        assertThat(userByEmail.getLastName()).isEqualTo("Doe");
        
        User nonExistentEmail = userRepository.findByEmail("nonexistent@example.com").orElse(null);
        assertThat(nonExistentEmail).isNull();
    }
}
