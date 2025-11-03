package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateProductRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.StorageService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.StripeService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/test")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Admin Test", description = "Admin test utilities")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminTestController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private StripeService stripeService;
    
    @Autowired
    private StorageService storageService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderRepository orderRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderItemRepository orderItemRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.PaymentRepository paymentRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.CartRepository cartRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.CartItemRepository cartItemRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.CartReservationRepository cartReservationRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.UserFavoriteRepository userFavoriteRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductReviewRepository productReviewRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.InventoryRepository inventoryRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.AddressRepository addressRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.VerificationTokenRepository verificationTokenRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.UserAnalyticsRepository userAnalyticsRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductAnalyticsRepository productAnalyticsRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.PageViewRepository pageViewRepository;
    
    @Autowired
    private andreas.kafkis.eberle.jewelry.shop.backend.repository.AnalyticsEventRepository analyticsEventRepository;
    
    @Value("${project.basedir:}")
    private String baseDir;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    
    /**
     * Create product in a completely new transaction to avoid rollback cascading
     * Each product creation is isolated - if one fails, others continue
     * ProductService.createProduct now has REQUIRES_NEW, so this just calls it directly
     */
    private ProductDTO createProductInTransaction(CreateProductRequest request) {
        // ProductService.createProduct has @Transactional(REQUIRES_NEW)
        // This ensures each product creation runs in its own isolated transaction
        return productService.createProduct(request);
    }
    
    /**
     * Create a MultipartFile implementation from file data
     */
    private MultipartFile createMultipartFile(String filename, String contentType, byte[] content) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return filename;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return content == null || content.length == 0;
            }

            @Override
            public long getSize() {
                return content != null ? content.length : 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return content != null ? content : new byte[0];
            }

            @Override
            public java.io.InputStream getInputStream() throws IOException {
                return new java.io.ByteArrayInputStream(content != null ? content : new byte[0]);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                if (content != null) {
                    Files.write(dest.toPath(), content);
                }
            }
        };
    }
    
    /**
     * Generate test products from JSON files
     */
    @PostMapping("/generate-products")
    @Operation(summary = "Generate test products from JSON files")
    public ResponseEntity<Map<String, Object>> generateTestProducts() {
        Map<String, Object> response = new HashMap<>();
        List<String> createdProducts = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // Try multiple possible paths for the test-products directory (always relative first)
            String baseDir = System.getProperty("user.dir");
            String[] possiblePaths = {
                "backend/src/test/resources/test-products",
                "src/test/resources/test-products",
                baseDir + "/backend/src/test/resources/test-products",
                baseDir + "/src/test/resources/test-products"
            };
            
            String testProductsPath = null;
            for (String path : possiblePaths) {
                if (Files.exists(Paths.get(path))) {
                    testProductsPath = path;
                    break;
                }
            }
            
            if (testProductsPath == null) {
                response.put("success", false);
                response.put("error", "Test products directory not found. Tried: " + String.join(", ", possiblePaths));
                return ResponseEntity.ok(response);
            }
            
            // Read product-1.json through product-5.json from their respective folders
            for (int i = 1; i <= 5; i++) {
                // Create product in separate transaction to avoid rollback cascading
                ProductDTO createdProductDTO = null;
                try {
                    String productFolder = testProductsPath + "/product-" + i;
                    String jsonPath = productFolder + "/product-" + i + ".json";
                    
                    // Check if folder and JSON file exist
                    Path productFolderPath = Paths.get(productFolder);
                    if (!Files.exists(productFolderPath) || !Files.isDirectory(productFolderPath)) {
                        errors.add("Product folder not found: product-" + i);
                        continue;
                    }
                    
                    if (!Files.exists(Paths.get(jsonPath))) {
                        errors.add("Product JSON not found: product-" + i + "/product-" + i + ".json");
                        continue;
                    }
                    
                    // Read JSON file
                    String jsonContent = new String(Files.readAllBytes(Paths.get(jsonPath)));
                    JsonNode productJson = objectMapper.readTree(jsonContent);
                    
                    // Build product request - prefix name with "TEST_"
                    CreateProductRequest request = CreateProductRequest.builder()
                        .name("TEST_" + productJson.get("name").asText())
                        .sku(productJson.get("sku").asText())
                        .description(productJson.has("description") ? productJson.get("description").asText() : null)
                        .price(new BigDecimal(productJson.get("price").asText()))
                        .baseCurrency(productJson.has("baseCurrency") ? productJson.get("baseCurrency").asText() : "CHF")
                        .quantity(productJson.has("quantity") ? productJson.get("quantity").asInt() : 0)
                        .weightGrams(productJson.has("weightGrams") ? new BigDecimal(productJson.get("weightGrams").asText()) : null)
                        .material(productJson.has("material") ? productJson.get("material").asText() : null)
                        .color(productJson.has("color") ? productJson.get("color").asText() : null)
                        .finish(productJson.has("finish") ? productJson.get("finish").asText() : null)
                        .ringSize(productJson.has("ringSize") ? productJson.get("ringSize").asText() : null)
                        .chainLength(productJson.has("chainLength") ? productJson.get("chainLength").asText() : null)
                        .gemstone(productJson.has("gemstone") ? productJson.get("gemstone").asText() : null)
                        .active(productJson.has("active") ? productJson.get("active").asBoolean() : true)
                        .specialOffer(productJson.has("specialOffer") ? productJson.get("specialOffer").asBoolean() : false)
                        .specialOfferPrice(productJson.has("specialOfferPrice") && !productJson.get("specialOfferPrice").isNull() 
                            ? new BigDecimal(productJson.get("specialOfferPrice").asText()) : null)
                        .specialOfferDescription(productJson.has("specialOfferDescriptions") && productJson.get("specialOfferDescriptions").isArray()
                            ? String.join(", ", productJson.get("specialOfferDescriptions").toString().replaceAll("[\\[\\]\"]", "").split(",")) : null)
                        .build();
                    
                    // Handle categories
                    if (productJson.has("category")) {
                        Set<String> categories = new HashSet<>();
                        categories.add(productJson.get("category").asText());
                        request.setCategories(categories);
                    }
                    
                    // Handle tags
                    if (productJson.has("tags") && productJson.get("tags").isArray()) {
                        Set<String> tags = new HashSet<>();
                        for (JsonNode tag : productJson.get("tags")) {
                            tags.add(tag.asText());
                        }
                        request.setTags(tags);
                    }
                    
                    // Create product in isolated transaction (catch any exceptions)
                    try {
                        log.info("Attempting to create product-{} with SKU: {}", i, request.getSku());
                        createdProductDTO = createProductInTransaction(request);
                        log.info("Successfully created product-{}: {}", i, createdProductDTO.getName());
                    } catch (IllegalArgumentException iae) {
                        // This is expected for duplicate SKU/name - log and continue
                        log.warn("Product-{} creation failed (duplicate or validation error): {}", i, iae.getMessage());
                        errors.add("Failed to create product-" + i + ": " + iae.getMessage() + " (duplicate SKU/name or validation error)");
                        continue; // Skip to next product
                    } catch (Exception createException) {
                        // Log the full exception for debugging with stack trace
                        log.error("Failed to create product-{} with detailed error:", i, createException);
                        String errorMsg = createException.getMessage();
                        if (createException.getCause() != null) {
                            errorMsg += " (Cause: " + createException.getCause().getMessage() + ")";
                        }
                        errors.add("Failed to create product-" + i + ": " + errorMsg);
                        continue; // Skip to next product
                    }
                    
                    if (createdProductDTO == null) {
                        errors.add("Failed to create product-" + i + ": Product creation returned null");
                        continue;
                    }
                    
                    // Find the created product entity to upload images
                    Product createdProduct = productRepository.findBySku(createdProductDTO.getSku())
                        .orElse(null);
                    
                    if (createdProduct == null) {
                        log.error("Created product not found in database for SKU: {}", createdProductDTO.getSku());
                        errors.add("Failed to find created product-" + i + " in database after creation");
                        continue;
                    }
                    
                    // Find all image files in the product folder (excluding JSON)
                    List<File> imageFiles = Files.list(productFolderPath)
                        .filter(path -> {
                            String filename = path.getFileName().toString().toLowerCase();
                            String ext = filename.substring(filename.lastIndexOf('.') + 1);
                            return !filename.endsWith(".json") && IMAGE_EXTENSIONS.contains(ext);
                        })
                        .sorted() // Sort for consistent ordering
                        .map(Path::toFile)
                        .collect(Collectors.toList());
                    
                    int uploadedImageCount = 0;
                    // Upload each image
                    for (int imgIndex = 0; imgIndex < imageFiles.size(); imgIndex++) {
                        File imageFile = imageFiles.get(imgIndex);
                        try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
                            // Read file content
                            byte[] fileBytes = fileInputStream.readAllBytes();
                            
                            // Determine content type
                            String contentType = Files.probeContentType(imageFile.toPath());
                            if (contentType == null) {
                                String filename = imageFile.getName().toLowerCase();
                                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                                    contentType = "image/jpeg";
                                } else if (filename.endsWith(".png")) {
                                    contentType = "image/png";
                                } else if (filename.endsWith(".gif")) {
                                    contentType = "image/gif";
                                } else if (filename.endsWith(".webp")) {
                                    contentType = "image/webp";
                                } else {
                                    contentType = "image/jpeg"; // Default
                                }
                            }
                            
                            // Create MultipartFile from file bytes using a proper implementation
                            MultipartFile multipartFile = createMultipartFile(
                                imageFile.getName(),
                                contentType,
                                fileBytes
                            );
                            
                            // Upload to storage
                            String storageKey = storageService.storeFileForProduct(
                                multipartFile,
                                createdProduct.getName(),
                                createdProduct.getId().toString()
                            );
                            String fileUrl = storageService.getFileUrl(storageKey);
                            
                            // Create ProductImage record
                            ProductImage productImage = ProductImage.builder()
                                .product(createdProduct)
                                .storageKey(storageKey)
                                .url(fileUrl)
                                .altText(createdProduct.getName() + " - Image " + (imgIndex + 1))
                                .isPrimary(imgIndex == 0) // First image is primary
                                .sortOrder(imgIndex + 1)
                                .width(800) // Default, could extract from image metadata
                                .height(800) // Default, could extract from image metadata
                                .mimeType(contentType)
                                .storageType(systemConfigService.getStorageType()) // Use configured storage type
                                .build();
                            
                            productImageRepository.save(productImage);
                            uploadedImageCount++;
                            log.info("Uploaded image {} for product {}: {}", imgIndex + 1, createdProduct.getName(), imageFile.getName());
                            
                        } catch (Exception e) {
                            errors.add("Failed to upload image " + imageFile.getName() + " for product-" + i + ": " + e.getMessage());
                            log.error("Failed to upload image {}: {}", imageFile.getName(), e.getMessage(), e);
                        }
                    }
                    
                    String productInfo = createdProduct.getName() + " (SKU: " + createdProduct.getSku() + ", " + uploadedImageCount + " images)";
                    createdProducts.add(productInfo);
                    log.info("Created test product: {} with {} images", createdProduct.getName(), uploadedImageCount);
                    
                } catch (Exception e) {
                    String error = "Failed to create product-" + i + ": " + e.getMessage();
                    errors.add(error);
                    log.error(error, e);
                }
            }
            
            response.put("success", errors.isEmpty());
            response.put("createdProducts", createdProducts);
            response.put("errors", errors);
            response.put("message", String.format("Created %d products. %d errors.", createdProducts.size(), errors.size()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating test products: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Delete all products, orders, transactions, and related data
     * Also cleans S3 bucket under products/ folder
     */
    @PostMapping("/delete-everything")
    @Operation(summary = "Delete all products, orders, transactions, and related data")
    public ResponseEntity<Map<String, Object>> deleteEverything() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.warn("Starting complete database cleanup - this will delete ALL products, orders, transactions, and related data");
            
            // Delete in order to respect foreign key constraints
            long deletedCounts = 0;
            
            // 1. Delete analytics data
            long analyticsEvents = analyticsEventRepository.count();
            analyticsEventRepository.deleteAll();
            deletedCounts += analyticsEvents;
            log.info("Deleted {} analytics events", analyticsEvents);
            
            long productAnalytics = productAnalyticsRepository.count();
            productAnalyticsRepository.deleteAll();
            deletedCounts += productAnalytics;
            log.info("Deleted {} product analytics", productAnalytics);
            
            long userAnalytics = userAnalyticsRepository.count();
            userAnalyticsRepository.deleteAll();
            deletedCounts += userAnalytics;
            log.info("Deleted {} user analytics", userAnalytics);
            
            long pageViews = pageViewRepository.count();
            pageViewRepository.deleteAll();
            deletedCounts += pageViews;
            log.info("Deleted {} page views", pageViews);
            
            // 2. Delete order-related data
            long orderItems = orderItemRepository.count();
            orderItemRepository.deleteAll();
            deletedCounts += orderItems;
            log.info("Deleted {} order items", orderItems);
            
            long payments = paymentRepository.count();
            paymentRepository.deleteAll();
            deletedCounts += payments;
            log.info("Deleted {} payments", payments);
            
            long orders = orderRepository.count();
            orderRepository.deleteAll();
            deletedCounts += orders;
            log.info("Deleted {} orders", orders);
            
            // 3. Delete cart-related data
            long cartReservations = cartReservationRepository.count();
            cartReservationRepository.deleteAll();
            deletedCounts += cartReservations;
            log.info("Deleted {} cart reservations", cartReservations);
            
            long cartItems = cartItemRepository.count();
            cartItemRepository.deleteAll();
            deletedCounts += cartItems;
            log.info("Deleted {} cart items", cartItems);
            
            long carts = cartRepository.count();
            cartRepository.deleteAll();
            deletedCounts += carts;
            log.info("Deleted {} carts", carts);
            
            // 4. Delete user favorites
            long favorites = userFavoriteRepository.count();
            userFavoriteRepository.deleteAll();
            deletedCounts += favorites;
            log.info("Deleted {} user favorites", favorites);
            
            // 5. Delete product reviews
            long reviews = productReviewRepository.count();
            productReviewRepository.deleteAll();
            deletedCounts += reviews;
            log.info("Deleted {} product reviews", reviews);
            
            // 6. Delete product images (before deleting products)
            long productImages = productImageRepository.count();
            productImageRepository.deleteAll();
            deletedCounts += productImages;
            log.info("Deleted {} product images", productImages);
            
            // 7. Delete inventory
            long inventory = inventoryRepository.count();
            inventoryRepository.deleteAll();
            deletedCounts += inventory;
            log.info("Deleted {} inventory records", inventory);
            
            // 8. Delete products (this will cascade delete product_tags, product_categories)
            long products = productRepository.count();
            productRepository.deleteAll();
            deletedCounts += products;
            log.info("Deleted {} products", products);
            
            // 9. Delete addresses (not user accounts, just addresses)
            long addresses = addressRepository.count();
            addressRepository.deleteAll();
            deletedCounts += addresses;
            log.info("Deleted {} addresses", addresses);
            
            // 10. Delete verification tokens
            long tokens = verificationTokenRepository.count();
            verificationTokenRepository.deleteAll();
            deletedCounts += tokens;
            log.info("Deleted {} verification tokens", tokens);
            
            // 11. Clean storage - delete all files under products/ folder (non-blocking)
            int s3DeletedCount = 0;
            String s3Error = null;
            try {
                Map<String, Object> s3Result = storageService.clearFolder("products");
                if (s3Result.containsKey("deletedCount")) {
                    s3DeletedCount = ((Number) s3Result.get("deletedCount")).intValue();
                } else if (s3Result.containsKey("totalDeletedCount")) {
                    s3DeletedCount = ((Number) s3Result.get("totalDeletedCount")).intValue();
                }
                log.info("✓ Successfully deleted {} files from storage products folder", s3DeletedCount);
            } catch (Exception e) {
                s3Error = e.getMessage();
                log.warn("⚠ Failed to clean storage (this is non-critical - database cleanup completed successfully): {}", e.getMessage());
                // Don't fail the entire operation if S3 deletion fails
            }
            
            // Build breakdown map with human-readable names
            Map<String, Long> breakdown = new HashMap<>();
            breakdown.put("Analytics Events", analyticsEvents);
            breakdown.put("Product Analytics", productAnalytics);
            breakdown.put("User Analytics", userAnalytics);
            breakdown.put("Page Views", pageViews);
            breakdown.put("Order Items", orderItems);
            breakdown.put("Payments", payments);
            breakdown.put("Orders", orders);
            breakdown.put("Cart Reservations", cartReservations);
            breakdown.put("Cart Items", cartItems);
            breakdown.put("Carts", carts);
            breakdown.put("User Favorites", favorites);
            breakdown.put("Product Reviews", reviews);
            breakdown.put("Product Images", productImages);
            breakdown.put("Inventory Records", inventory);
            breakdown.put("Products", products);
            breakdown.put("Addresses", addresses);
            breakdown.put("Verification Tokens", tokens);
            
            // Build detailed summary message
            StringBuilder summaryMessage = new StringBuilder();
            summaryMessage.append(String.format("Successfully deleted %d database records", deletedCounts));
            if (s3DeletedCount > 0) {
                summaryMessage.append(String.format(" and %d storage files", s3DeletedCount));
            }
            if (s3Error != null) {
                summaryMessage.append("\n⚠ Storage cleanup failed (database cleanup completed): " + s3Error);
            }
            
            response.put("success", true);
            response.put("message", summaryMessage.toString());
            response.put("deletedDatabaseRecords", deletedCounts);
            response.put("deletedS3Files", s3DeletedCount);
            response.put("s3Error", s3Error); // Include S3 error info if any
            response.put("breakdown", breakdown);
            
            // Enhanced logging with detailed breakdown
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("✓ COMPLETE DATABASE CLEANUP FINISHED SUCCESSFULLY");
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("Total Database Records Deleted: {}", deletedCounts);
            log.info("───────────────────────────────────────────────────────────────");
            log.info("BREAKDOWN BY TABLE:");
            log.info("  • Analytics Events:        {}", analyticsEvents);
            log.info("  • Product Analytics:       {}", productAnalytics);
            log.info("  • User Analytics:          {}", userAnalytics);
            log.info("  • Page Views:               {}", pageViews);
            log.info("  • Order Items:              {}", orderItems);
            log.info("  • Payments:                 {}", payments);
            log.info("  • Orders:                   {}", orders);
            log.info("  • Cart Reservations:        {}", cartReservations);
            log.info("  • Cart Items:               {}", cartItems);
            log.info("  • Carts:                    {}", carts);
            log.info("  • User Favorites:           {}", favorites);
            log.info("  • Product Reviews:          {}", reviews);
            log.info("  • Product Images:           {}", productImages);
            log.info("  • Inventory Records:        {}", inventory);
            log.info("  • Products:                 {}", products);
            log.info("  • Addresses:                {}", addresses);
            log.info("  • Verification Tokens:      {}", tokens);
            log.info("───────────────────────────────────────────────────────────────");
            if (s3DeletedCount > 0) {
                log.info("Storage Files Deleted: {}", s3DeletedCount);
            } else if (s3Error != null) {
                log.warn("⚠ Storage cleanup failed: {}", s3Error);
                log.warn("   Note: Database cleanup completed successfully.");
            }
            log.info("═══════════════════════════════════════════════════════════════");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting everything: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Failed to delete all data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Test Stripe connection and create a test transaction (TEST/SANDBOX mode)
     * Creates a minimal PaymentIntent to verify the connection works
     */
    @PostMapping("/test-stripe")
    @Operation(summary = "Test Stripe TEST/SANDBOX connection by creating a test PaymentIntent")
    public ResponseEntity<Map<String, Object>> testStripe() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isConfigured = stripeService.isConfigured();
            String publishableKey = stripeService.getPublishableKey();
            String secretKey = stripeService.getStripeSecretKeyPublic();
            
            response.put("configured", isConfigured);
            response.put("hasPublishableKey", publishableKey != null && !publishableKey.isEmpty());
            response.put("publishableKeyPreview", publishableKey != null && publishableKey.length() > 10 
                ? publishableKey.substring(0, 10) + "..." : "Not set");
            
            // Detect environment based on key prefix
            boolean isTestMode = secretKey != null && secretKey.startsWith("sk_test_");
            boolean isLiveMode = secretKey != null && secretKey.startsWith("sk_live_");
            
            response.put("environment", isTestMode ? "test" : (isLiveMode ? "live" : "unknown"));
            
            if (!isConfigured) {
                response.put("status", "warning");
                response.put("message", "Stripe is not configured. Please configure API keys in settings.");
                return ResponseEntity.ok(response);
            }
            
            // Create a test PaymentIntent with minimal amount ($0.50 USD = 50 cents)
            // This will appear in Stripe Dashboard -> Payments to verify the connection
            try {
                Map<String, Object> testPaymentIntent = stripeService.createTestPaymentIntent(
                    isTestMode ? "test" : "live"
                );
                
                response.put("status", "success");
                response.put("testPaymentIntentId", testPaymentIntent.get("paymentIntentId"));
                response.put("testPaymentIntentStatus", testPaymentIntent.get("status"));
                response.put("testAmount", testPaymentIntent.get("amount"));
                response.put("testCurrency", testPaymentIntent.get("currency"));
                String note = testPaymentIntent.containsKey("note") ? "\n" + testPaymentIntent.get("note") : "";
                response.put("message", String.format(
                    "✓ Stripe %s connection verified! Test PaymentIntent created: %s (%.2f %s).%s Check your Stripe Dashboard -> Payments to see it.",
                    isTestMode ? "TEST/SANDBOX" : "LIVE",
                    testPaymentIntent.get("paymentIntentId"),
                    testPaymentIntent.get("amount"),
                    testPaymentIntent.get("currency"),
                    note
                ));
                response.put("dashboardUrl", isTestMode 
                    ? "https://dashboard.stripe.com/test/payments"
                    : "https://dashboard.stripe.com/payments");
                response.put("modeLabel", isTestMode ? "SANDBOX (TEST)" : "PRODUCTION (LIVE)");
                
                log.info("Test PaymentIntent created successfully: {} in {} mode", 
                    testPaymentIntent.get("paymentIntentId"), isTestMode ? "TEST" : "LIVE");
                
            } catch (Exception e) {
                log.error("Failed to create test PaymentIntent: {}", e.getMessage(), e);
                response.put("status", "error");
                response.put("message", "Stripe keys are configured but test transaction failed: " + e.getMessage());
                response.put("error", e.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error testing Stripe: {}", e.getMessage(), e);
            response.put("configured", false);
            response.put("status", "error");
            response.put("message", "Error checking Stripe configuration: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Test Stripe connection and create a test transaction (PRODUCTION/LIVE mode)
     * Creates a minimal PaymentIntent to verify the LIVE connection works
     * WARNING: This will create a real transaction in production mode
     */
    @PostMapping("/test-stripe-prod")
    @Operation(summary = "Test Stripe PRODUCTION/LIVE connection by creating a test PaymentIntent")
    public ResponseEntity<Map<String, Object>> testStripeProd() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isConfigured = stripeService.isConfigured();
            String publishableKey = stripeService.getPublishableKey();
            String secretKey = stripeService.getStripeSecretKeyPublic();
            
            response.put("configured", isConfigured);
            response.put("hasPublishableKey", publishableKey != null && !publishableKey.isEmpty());
            response.put("publishableKeyPreview", publishableKey != null && publishableKey.length() > 10 
                ? publishableKey.substring(0, 10) + "..." : "Not set");
            
            // Force LIVE mode check
            boolean isLiveMode = secretKey != null && secretKey.startsWith("sk_live_");
            
            response.put("environment", "live");
            response.put("modeLabel", "PRODUCTION (LIVE)");
            
            if (!isConfigured) {
                response.put("status", "warning");
                response.put("message", "Stripe is not configured. Please configure API keys in settings.");
                return ResponseEntity.ok(response);
            }
            
            if (!isLiveMode) {
                response.put("status", "warning");
                response.put("message", "⚠️ LIVE mode keys (sk_live_*) are not configured. Current keys appear to be TEST keys (sk_test_*).");
                response.put("warning", "To test production mode, configure LIVE keys in settings first.");
                return ResponseEntity.ok(response);
            }
            
            // Create a test PaymentIntent in LIVE mode with currency-appropriate minimum
            try {
                // Get default currency from config
                String defaultCurrency = systemConfigService.getConfigValue("app.currency.default");
                if (defaultCurrency == null || defaultCurrency.isEmpty()) {
                    defaultCurrency = "CHF"; // Fallback
                }
                
                Map<String, Object> testPaymentIntent = stripeService.createTestPaymentIntent(
                    "live",
                    defaultCurrency
                );
                
                response.put("status", "success");
                response.put("testPaymentIntentId", testPaymentIntent.get("paymentIntentId"));
                response.put("testPaymentIntentStatus", testPaymentIntent.get("status"));
                response.put("testAmount", testPaymentIntent.get("amount"));
                response.put("testCurrency", testPaymentIntent.get("currency"));
                response.put("message", String.format(
                    "✓ Stripe PRODUCTION (LIVE) connection verified! Test PaymentIntent created: %s (%.2f %s). ⚠️ This is a REAL transaction - check your Stripe Dashboard -> Payments.",
                    testPaymentIntent.get("paymentIntentId"),
                    testPaymentIntent.get("amount"),
                    testPaymentIntent.get("currency")
                ));
                response.put("dashboardUrl", "https://dashboard.stripe.com/payments");
                response.put("warning", "⚠️ This test created a REAL transaction in LIVE mode. Check your Stripe Dashboard to confirm.");
                
                log.warn("LIVE MODE test PaymentIntent created: {} - This is a REAL transaction!", 
                    testPaymentIntent.get("paymentIntentId"));
                
            } catch (Exception e) {
                log.error("Failed to create LIVE test PaymentIntent: {}", e.getMessage(), e);
                response.put("status", "error");
                response.put("message", "Stripe LIVE keys are configured but test transaction failed: " + e.getMessage());
                response.put("error", e.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error testing Stripe LIVE: {}", e.getMessage(), e);
            response.put("configured", false);
            response.put("status", "error");
            response.put("message", "Error checking Stripe LIVE configuration: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}

