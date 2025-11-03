package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateDiscountCodeRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.DiscountCodeDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ValidateDiscountCodeResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.DiscountCode;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.DiscountCodeUsage;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.DiscountCodeRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.DiscountCodeUsageRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiscountCodeService {
    
    @Autowired
    private DiscountCodeRepository discountCodeRepository;
    
    @Autowired
    private DiscountCodeUsageRepository discountCodeUsageRepository;
    
    /**
     * Create a new discount code
     */
    @Transactional
    public DiscountCodeDTO createDiscountCode(CreateDiscountCodeRequest request) {
        // Check if code already exists
        if (discountCodeRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new RuntimeException("Discount code already exists: " + request.getCode());
        }
        
        DiscountCode discountCode = DiscountCode.builder()
                .code(request.getCode().toUpperCase().trim())
                .description(request.getDescription())
                .discountType(DiscountCode.DiscountType.valueOf(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .minimumPurchaseAmount(request.getMinimumPurchaseAmount() != null ? request.getMinimumPurchaseAmount() : BigDecimal.ZERO)
                .maximumDiscountAmount(request.getMaximumDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();
        
        DiscountCode saved = discountCodeRepository.save(discountCode);
        log.info("Created discount code: {}", saved.getCode());
        
        return convertToDTO(saved);
    }
    
    /**
     * Get all discount codes
     */
    public List<DiscountCodeDTO> getAllDiscountCodes() {
        return discountCodeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get discount code by ID
     */
    public DiscountCodeDTO getDiscountCodeById(UUID id) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount code not found: " + id));
        return convertToDTO(discountCode);
    }
    
    /**
     * Update discount code
     */
    @Transactional
    public DiscountCodeDTO updateDiscountCode(UUID id, CreateDiscountCodeRequest request) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount code not found: " + id));
        
        // Check if code already exists (and is not the current one)
        if (!discountCode.getCode().equalsIgnoreCase(request.getCode().trim())) {
            if (discountCodeRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
                throw new RuntimeException("Discount code already exists: " + request.getCode());
            }
        }
        
        discountCode.setCode(request.getCode().toUpperCase().trim());
        discountCode.setDescription(request.getDescription());
        discountCode.setDiscountType(DiscountCode.DiscountType.valueOf(request.getDiscountType()));
        discountCode.setDiscountValue(request.getDiscountValue());
        discountCode.setMinimumPurchaseAmount(request.getMinimumPurchaseAmount() != null ? request.getMinimumPurchaseAmount() : BigDecimal.ZERO);
        discountCode.setMaximumDiscountAmount(request.getMaximumDiscountAmount());
        discountCode.setUsageLimit(request.getUsageLimit());
        discountCode.setIsActive(request.getIsActive() != null ? request.getIsActive() : discountCode.getIsActive());
        discountCode.setValidFrom(request.getValidFrom());
        discountCode.setValidUntil(request.getValidUntil());
        
        DiscountCode saved = discountCodeRepository.save(discountCode);
        log.info("Updated discount code: {}", saved.getCode());
        
        return convertToDTO(saved);
    }
    
    /**
     * Delete discount code
     */
    @Transactional
    public void deleteDiscountCode(UUID id) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount code not found: " + id));
        discountCodeRepository.delete(discountCode);
        log.info("Deleted discount code: {}", discountCode.getCode());
    }
    
    /**
     * Validate and calculate discount for a given code and amount
     */
    public ValidateDiscountCodeResponse validateDiscountCode(String code, BigDecimal orderAmount, User user) {
        OffsetDateTime now = OffsetDateTime.now();
        
        DiscountCode discountCode = discountCodeRepository.findByCodeIgnoreCase(code)
                .orElse(null);
        
        if (discountCode == null) {
            return ValidateDiscountCodeResponse.builder()
                    .valid(false)
                    .message("Invalid discount code")
                    .build();
        }
        
        // Check if active
        if (!discountCode.getIsActive()) {
            return ValidateDiscountCodeResponse.builder()
                    .valid(false)
                    .message("Discount code is not active")
                    .build();
        }
        
        // Check validity dates
        if (discountCode.getValidFrom() != null && now.isBefore(discountCode.getValidFrom())) {
            return ValidateDiscountCodeResponse.builder()
                    .valid(false)
                    .message("Discount code is not yet valid")
                    .build();
        }
        
        if (discountCode.getValidUntil() != null && now.isAfter(discountCode.getValidUntil())) {
            return ValidateDiscountCodeResponse.builder()
                    .valid(false)
                    .message("Discount code has expired")
                    .build();
        }
        
        // Check minimum purchase amount
        if (discountCode.getMinimumPurchaseAmount() != null && 
            orderAmount.compareTo(discountCode.getMinimumPurchaseAmount()) < 0) {
            return ValidateDiscountCodeResponse.builder()
                    .valid(false)
                    .message(String.format("Minimum purchase amount of %s required", discountCode.getMinimumPurchaseAmount()))
                    .build();
        }
        
        // Check usage limit
        if (discountCode.getUsageLimit() != null && discountCode.getUsageCount() >= discountCode.getUsageLimit()) {
            return ValidateDiscountCodeResponse.builder()
                    .valid(false)
                    .message("Discount code has reached its usage limit")
                    .build();
        }
        
        // Calculate discount amount
        BigDecimal discountAmount;
        if (discountCode.getDiscountType() == DiscountCode.DiscountType.PERCENTAGE) {
            discountAmount = orderAmount.multiply(discountCode.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            // Apply maximum discount if set
            if (discountCode.getMaximumDiscountAmount() != null && 
                discountAmount.compareTo(discountCode.getMaximumDiscountAmount()) > 0) {
                discountAmount = discountCode.getMaximumDiscountAmount();
            }
        } else {
            discountAmount = discountCode.getDiscountValue();
        }
        
        // Ensure discount doesn't exceed order amount
        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }
        
        BigDecimal discountedAmount = orderAmount.subtract(discountAmount);
        
        return ValidateDiscountCodeResponse.builder()
                .valid(true)
                .message("Discount code applied successfully")
                .discountAmount(discountAmount)
                .discountType(discountCode.getDiscountType().name())
                .originalAmount(orderAmount)
                .discountedAmount(discountedAmount)
                .build();
    }
    
    /**
     * Apply discount code to an order (record usage)
     */
    @Transactional
    public void applyDiscountCodeToOrder(String code, Order order, User user, BigDecimal discountAmount) {
        DiscountCode discountCode = discountCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Discount code not found: " + code));
        
        // Increment usage count
        discountCode.setUsageCount(discountCode.getUsageCount() + 1);
        discountCodeRepository.save(discountCode);
        
        // Record usage
        DiscountCodeUsage usage = DiscountCodeUsage.builder()
                .discountCode(discountCode)
                .user(user)
                .order(order)
                .discountAmount(discountAmount)
                .build();
        
        discountCodeUsageRepository.save(usage);
        log.info("Applied discount code {} to order {}", code, order.getOrderNumber());
    }
    
    /**
     * Convert entity to DTO
     */
    private DiscountCodeDTO convertToDTO(DiscountCode discountCode) {
        return DiscountCodeDTO.builder()
                .id(discountCode.getId())
                .code(discountCode.getCode())
                .description(discountCode.getDescription())
                .discountType(discountCode.getDiscountType().name())
                .discountValue(discountCode.getDiscountValue())
                .minimumPurchaseAmount(discountCode.getMinimumPurchaseAmount())
                .maximumDiscountAmount(discountCode.getMaximumDiscountAmount())
                .usageLimit(discountCode.getUsageLimit())
                .usageCount(discountCode.getUsageCount())
                .isActive(discountCode.getIsActive())
                .validFrom(discountCode.getValidFrom())
                .validUntil(discountCode.getValidUntil())
                .createdAt(discountCode.getCreatedAt())
                .updatedAt(discountCode.getUpdatedAt())
                .build();
    }
}

