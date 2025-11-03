package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Stripe Payment", description = "Stripe payment integration endpoints")
public class StripeController {
    
    @Autowired
    private StripeService stripeService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * Get Stripe publishable key
     */
    @GetMapping("/config")
    @Operation(summary = "Get Stripe configuration", description = "Returns publishable key for frontend")
    public ResponseEntity<Map<String, Object>> getStripeConfig() {
        try {
            String publishableKey = stripeService.getPublishableKey();
            boolean isConfigured = stripeService.isConfigured();
            
            return ResponseEntity.ok(Map.of(
                "publishableKey", publishableKey != null ? publishableKey : "",
                "configured", isConfigured
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "publishableKey", "",
                "configured", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Create payment intent for an order
     */
    @PostMapping("/create-payment-intent")
    @Operation(summary = "Create payment intent", description = "Create a Stripe payment intent for an order")
    public ResponseEntity<?> createPaymentIntent(
            @RequestParam UUID orderId,
            @RequestParam(defaultValue = "CHF") String currency,
            Authentication authentication) {
        try {
            // Find order
            var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Verify order belongs to authenticated user
            if (authentication != null) {
                String userEmail = authentication.getName();
                if (!order.getCustomer().getEmail().equals(userEmail)) {
                    return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
                }
            }
            
            Map<String, Object> paymentIntent = stripeService.createPaymentIntent(order, currency);
            
            return ResponseEntity.ok(paymentIntent);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Confirm payment intent
     */
    @PostMapping("/confirm-payment")
    @Operation(summary = "Confirm payment", description = "Confirm a Stripe payment intent")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, String> request) {
        try {
            String paymentIntentId = request.get("paymentIntentId");
            
            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "paymentIntentId is required"));
            }
            
            Map<String, Object> result = stripeService.confirmPaymentIntent(paymentIntentId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get payment status
     */
    @GetMapping("/payment-status")
    @Operation(summary = "Get payment status", description = "Get status of a payment intent")
    public ResponseEntity<?> getPaymentStatus(@RequestParam String paymentIntentId) {
        try {
            Map<String, Object> status = stripeService.getPaymentIntentStatus(paymentIntentId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

