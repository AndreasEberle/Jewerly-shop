package andreas.kafkis.eberle.jewelry.shop.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateImageRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProductImageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = Product.builder()
                .sku("INTEGRATION-TEST-001")
                .name("Integration Test Jewelry")
                .description("Product for integration testing")
                .priceCents(15000L)
                .currency("EUR")
                .material("Silver")
                .gemstone("Ruby")
                .weightGrams(BigDecimal.valueOf(8.5))
                .active(true)
                .build();
        
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testCompleteImageWorkflow() throws Exception {
        // Step 1: Upload image file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "integration-test.webp",
                "image/webp",
                "fake image content for integration test".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/admin/upload/{productId}", testProduct.getId())
                .file(file))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(uploadResponse).contains("products/" + testProduct.getId() + "/");

        // Step 2: Create image record in database
        CreateImageRequest createRequest = new CreateImageRequest();
        createRequest.setStorageKey(uploadResponse);
        createRequest.setUrl("http://localhost:8080/files/" + uploadResponse);
        createRequest.setPrimary(true);
        createRequest.setSortOrder(0);
        createRequest.setAltText("Integration test image");
        createRequest.setWidth(800);
        createRequest.setHeight(600);
        createRequest.setMimeType("image/webp");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/products/{productId}/images", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primary").value(true))
                .andExpect(jsonPath("$.altText").value("Integration test image"));

        // Step 3: Retrieve images for product
        mockMvc.perform(get("/api/products/{productId}/images", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].primary").value(true))
                .andExpect(jsonPath("$[0].altText").value("Integration test image"));

        // Step 4: Get primary image
        mockMvc.perform(get("/api/products/{productId}/images/primary", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primary").value(true))
                .andExpect(jsonPath("$.altText").value("Integration test image"));

        // Verify database state
        assertThat(productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(testProduct.getId()))
                .hasSize(1)
                .allMatch(image -> image.isPrimary())
                .allMatch(image -> image.getAltText().equals("Integration test image"));
    }

    @Test
    void testImageNotFoundScenarios() throws Exception {
        UUID nonExistentProductId = UUID.randomUUID();

        // Test getting images for non-existent product
        mockMvc.perform(get("/api/products/{productId}/images", nonExistentProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Test getting primary image for non-existent product
        mockMvc.perform(get("/api/products/{productId}/images/primary", nonExistentProductId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testMultipleImagesOrdering() throws Exception {
        // Create multiple images with different sort orders
        ProductImage image1 = ProductImage.builder()
                .product(testProduct)
                .storageKey("products/" + testProduct.getId() + "/image1.webp")
                .isPrimary(false)
                .sortOrder(2)
                .altText("Image 1")
                .width(400)
                .height(300)
                .mimeType("image/webp")
                .createdAt(OffsetDateTime.now())
                .build();

        ProductImage image2 = ProductImage.builder()
                .product(testProduct)
                .storageKey("products/" + testProduct.getId() + "/image2.webp")
                .isPrimary(true)
                .sortOrder(0)
                .altText("Image 2")
                .width(800)
                .height(600)
                .mimeType("image/webp")
                .createdAt(OffsetDateTime.now())
                .build();

        ProductImage image3 = ProductImage.builder()
                .product(testProduct)
                .storageKey("products/" + testProduct.getId() + "/image3.webp")
                .isPrimary(false)
                .sortOrder(1)
                .altText("Image 3")
                .width(600)
                .height(450)
                .mimeType("image/webp")
                .createdAt(OffsetDateTime.now())
                .build();

        productImageRepository.save(image1);
        productImageRepository.save(image2);
        productImageRepository.save(image3);

        // Test ordering: primary first, then by sort order
        mockMvc.perform(get("/api/products/{productId}/images", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].primary").value(true))  // Primary first
                .andExpect(jsonPath("$[0].sortOrder").value(0))
                .andExpect(jsonPath("$[1].primary").value(false)) // Then by sort order
                .andExpect(jsonPath("$[1].sortOrder").value(1))
                .andExpect(jsonPath("$[2].primary").value(false))
                .andExpect(jsonPath("$[2].sortOrder").value(2));
    }
}
