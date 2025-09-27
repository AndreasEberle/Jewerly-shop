package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Process payment
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Refund payment (Admin only)
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable UUID paymentId,
            @RequestParam BigDecimal amount
    ) {
        PaymentResponse response = paymentService.refundPayment(paymentId, amount);
        return ResponseEntity.ok(response);
    }

    /**
     * Get supported payment methods
     */
    @GetMapping("/methods")
    public ResponseEntity<Object> getPaymentMethods() {
        return ResponseEntity.ok(java.util.Map.of(
                "methods", java.util.List.of(
                        java.util.Map.of("id", "stripe", "name", "Credit Card (Stripe)", "enabled", true),
                        java.util.Map.of("id", "paypal", "name", "PayPal", "enabled", true),
                        java.util.Map.of("id", "demo", "name", "Demo Payment", "enabled", true)
                )
        ));
    }
}
