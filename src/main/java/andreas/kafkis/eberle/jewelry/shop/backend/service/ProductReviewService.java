package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateProductReviewRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductReviewDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductReviewStatsDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductReview;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductReviewRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;

@Service
@Transactional
public class ProductReviewService {
    
    @Autowired
    private ProductReviewRepository productReviewRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new product review
     */
    public ProductReviewDTO createReview(UUID userId, UUID productId, CreateProductReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if user already reviewed this product
        if (productReviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("You have already reviewed this product");
        }
        
        ProductReview review = ProductReview.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .isVerifiedPurchase(request.getIsVerifiedPurchase() != null ? request.getIsVerifiedPurchase() : false)
                .isApproved(true) // Auto-approve for now
                .build();
        
        ProductReview savedReview = productReviewRepository.save(review);
        return convertToDTO(savedReview);
    }
    
    /**
     * Get reviews for a product
     */
    @Transactional(readOnly = true)
    public List<ProductReviewDTO> getProductReviews(UUID productId) {
        List<ProductReview> reviews = productReviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get paginated reviews for a product
     */
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getProductReviews(UUID productId, Pageable pageable) {
        Page<ProductReview> reviews = productReviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable);
        return reviews.map(this::convertToDTO);
    }
    
    /**
     * Get review statistics for a product
     */
    @Transactional(readOnly = true)
    public ProductReviewStatsDTO getProductReviewStats(UUID productId) {
        Double averageRating = productReviewRepository.findAverageRatingByProductId(productId);
        Long totalReviews = productReviewRepository.countByProductIdAndIsApprovedTrue(productId);
        
        Long rating1Count = productReviewRepository.countByProductIdAndRatingAndIsApprovedTrue(productId, 1);
        Long rating2Count = productReviewRepository.countByProductIdAndRatingAndIsApprovedTrue(productId, 2);
        Long rating3Count = productReviewRepository.countByProductIdAndRatingAndIsApprovedTrue(productId, 3);
        Long rating4Count = productReviewRepository.countByProductIdAndRatingAndIsApprovedTrue(productId, 4);
        Long rating5Count = productReviewRepository.countByProductIdAndRatingAndIsApprovedTrue(productId, 5);
        
        Long verifiedPurchaseCount = productReviewRepository.countByProductIdAndIsVerifiedPurchaseTrueAndIsApprovedTrue(productId);
        
        return ProductReviewStatsDTO.builder()
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews.intValue())
                .rating1Count(rating1Count.intValue())
                .rating2Count(rating2Count.intValue())
                .rating3Count(rating3Count.intValue())
                .rating4Count(rating4Count.intValue())
                .rating5Count(rating5Count.intValue())
                .verifiedPurchaseCount(verifiedPurchaseCount.intValue())
                .build();
    }
    
    /**
     * Get user's reviews
     */
    @Transactional(readOnly = true)
    public List<ProductReviewDTO> getUserReviews(UUID userId) {
        List<ProductReview> reviews = productReviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get paginated user reviews
     */
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getUserReviews(UUID userId, Pageable pageable) {
        Page<ProductReview> reviews = productReviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return reviews.map(this::convertToDTO);
    }
    
    /**
     * Update a review
     */
    public ProductReviewDTO updateReview(UUID userId, UUID reviewId, CreateProductReviewRequest request) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own reviews");
        }
        
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setIsVerifiedPurchase(request.getIsVerifiedPurchase() != null ? request.getIsVerifiedPurchase() : false);
        
        ProductReview savedReview = productReviewRepository.save(review);
        return convertToDTO(savedReview);
    }
    
    /**
     * Delete a review
     */
    public void deleteReview(UUID userId, UUID reviewId) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own reviews");
        }
        
        productReviewRepository.delete(review);
    }
    
    /**
     * Convert ProductReview entity to DTO
     */
    private ProductReviewDTO convertToDTO(ProductReview review) {
        return ProductReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .userEmail(review.getUser().getEmail())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .isApproved(review.getIsApproved())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
