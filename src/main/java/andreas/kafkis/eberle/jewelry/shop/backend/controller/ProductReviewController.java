package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateProductReviewRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductReviewDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductReviewStatsDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductReviewService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class ProductReviewController {
    
    @Autowired
    private ProductReviewService productReviewService;
    
    /**
     * Get reviews for a product
     */
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<List<ProductReviewDTO>> getProductReviews(@PathVariable UUID productId) {
        List<ProductReviewDTO> reviews = productReviewService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get paginated reviews for a product
     */
    @GetMapping("/{productId}/reviews/paginated")
    public ResponseEntity<Page<ProductReviewDTO>> getProductReviews(@PathVariable UUID productId, Pageable pageable) {
        Page<ProductReviewDTO> reviews = productReviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get review statistics for a product
     */
    @GetMapping("/{productId}/reviews/stats")
    public ResponseEntity<ProductReviewStatsDTO> getProductReviewStats(@PathVariable UUID productId) {
        ProductReviewStatsDTO stats = productReviewService.getProductReviewStats(productId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Create a new review
     */
    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ProductReviewDTO> createReview(
            @PathVariable UUID productId, 
            @RequestBody CreateProductReviewRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProductReviewDTO review = productReviewService.createReview(user.getId(), productId, request);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Update a review
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ProductReviewDTO> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody CreateProductReviewRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProductReviewDTO review = productReviewService.updateReview(user.getId(), reviewId, request);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Delete a review
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable UUID reviewId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        productReviewService.deleteReview(user.getId(), reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }
    
    /**
     * Get user's reviews
     */
    @GetMapping("/user/reviews")
    public ResponseEntity<List<ProductReviewDTO>> getUserReviews(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ProductReviewDTO> reviews = productReviewService.getUserReviews(user.getId());
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get paginated user reviews
     */
    @GetMapping("/user/reviews/paginated")
    public ResponseEntity<Page<ProductReviewDTO>> getUserReviews(Authentication authentication, Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        Page<ProductReviewDTO> reviews = productReviewService.getUserReviews(user.getId(), pageable);
        return ResponseEntity.ok(reviews);
    }
}



