package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateDiscountCodeRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.DiscountCodeDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ValidateDiscountCodeResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.DiscountCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Discount Codes", description = "Discount code management and validation")
@Slf4j
public class DiscountCodeController {
    
    @Autowired
    private DiscountCodeService discountCodeService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Validate discount code (public endpoint)
     */
    @PostMapping("/public/discount-codes/validate")
    @Operation(summary = "Validate a discount code")
    public ResponseEntity<ValidateDiscountCodeResponse> validateDiscountCode(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount,
            Authentication authentication) {
        try {
            UUID userId = null;
            if (authentication != null) {
                String email = authentication.getName();
                userId = userRepository.findByEmail(email) != null ? 
                    userRepository.findByEmail(email).getId() : null;
            }
            
            // For now, we don't check per-user usage limits
            // You can add that check here if needed
            
            ValidateDiscountCodeResponse response = discountCodeService.validateDiscountCode(
                code, orderAmount, userId != null ? userRepository.findById(userId).orElse(null) : null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating discount code: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ValidateDiscountCodeResponse.builder()
                    .valid(false)
                    .message("Error validating discount code: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get all discount codes (admin only)
     */
    @GetMapping("/admin/discount-codes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all discount codes")
    public ResponseEntity<List<DiscountCodeDTO>> getAllDiscountCodes() {
        try {
            List<DiscountCodeDTO> codes = discountCodeService.getAllDiscountCodes();
            return ResponseEntity.ok(codes);
        } catch (Exception e) {
            log.error("Error getting discount codes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get discount code by ID (admin only)
     */
    @GetMapping("/admin/discount-codes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get discount code by ID")
    public ResponseEntity<DiscountCodeDTO> getDiscountCodeById(@PathVariable UUID id) {
        try {
            DiscountCodeDTO code = discountCodeService.getDiscountCodeById(id);
            return ResponseEntity.ok(code);
        } catch (Exception e) {
            log.error("Error getting discount code: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create discount code (admin only)
     */
    @PostMapping("/admin/discount-codes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new discount code")
    public ResponseEntity<DiscountCodeDTO> createDiscountCode(@Valid @RequestBody CreateDiscountCodeRequest request) {
        try {
            DiscountCodeDTO code = discountCodeService.createDiscountCode(request);
            return ResponseEntity.ok(code);
        } catch (Exception e) {
            log.error("Error creating discount code: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update discount code (admin only)
     */
    @PutMapping("/admin/discount-codes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a discount code")
    public ResponseEntity<DiscountCodeDTO> updateDiscountCode(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDiscountCodeRequest request) {
        try {
            DiscountCodeDTO code = discountCodeService.updateDiscountCode(id, request);
            return ResponseEntity.ok(code);
        } catch (Exception e) {
            log.error("Error updating discount code: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete discount code (admin only)
     */
    @DeleteMapping("/admin/discount-codes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a discount code")
    public ResponseEntity<Void> deleteDiscountCode(@PathVariable UUID id) {
        try {
            discountCodeService.deleteDiscountCode(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting discount code: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}


