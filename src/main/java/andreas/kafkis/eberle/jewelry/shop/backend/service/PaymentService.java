package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Payment;
import andreas.kafkis.eberle.jewelry.shop.backend.exception.ResourceNotFoundException;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.PaymentRepository;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmailService emailService;

    @Value("${payment.stripe.secret-key:sk_test_demo}")
    private String stripeSecretKey;

    @Value("${payment.paypal.client-id:demo_client_id}")
    private String paypalClientId;

    /**
     * Process payment
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        // Get order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> ResourceNotFoundException.forOrder(request.getOrderId().toString()));

        // Validate payment amount
        if (request.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Payment amount does not match order total");
        }

        // Process payment based on method
        PaymentResponse response;
        switch (request.getPaymentMethod().toLowerCase()) {
            case "stripe":
                response = processStripePayment(request);
                break;
            case "paypal":
                response = processPayPalPayment(request);
                break;
            case "demo":
                response = processDemoPayment(request);
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
        }

        // Save payment record
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getAmount())
                .status(Payment.PaymentStatus.valueOf(response.getStatus()))
                .transactionId(response.getTransactionId())
                .processedAt(response.getProcessedAt())
                .failureReason(response.getFailureReason())
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);

        // Update order status if payment successful
        if ("SUCCESS".equals(response.getStatus())) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setUpdatedAt(LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC));
            orderRepository.save(order);
            
            // Send payment confirmation email
            emailService.sendPaymentConfirmation(order, response.getTransactionId());
        }

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(order.getId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus().toString())
                .transactionId(payment.getTransactionId())
                .processedAt(payment.getProcessedAt())
                .failureReason(payment.getFailureReason())
                .notes(payment.getNotes())
                .build();
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.forOrder(paymentId.toString()));

        return convertToPaymentResponse(payment);
    }

    /**
     * Refund payment
     */
    public PaymentResponse refundPayment(UUID paymentId, BigDecimal amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.forOrder(paymentId.toString()));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Cannot refund unsuccessful payment");
        }

        // Process refund based on payment method
        PaymentResponse refundResponse;
        switch (payment.getPaymentMethod().toLowerCase()) {
            case "stripe":
                refundResponse = processStripeRefund(payment, amount);
                break;
            case "paypal":
                refundResponse = processPayPalRefund(payment, amount);
                break;
            case "demo":
                refundResponse = processDemoRefund(payment, amount);
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment method for refund");
        }

        // Update payment status
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return refundResponse;
    }

    // Payment method implementations

    private PaymentResponse processStripePayment(PaymentRequest request) {
        // In real implementation, integrate with Stripe API
        // For demo purposes, simulate processing
        try {
            // Simulate API call delay
            Thread.sleep(1000);
            
            // Simulate success/failure based on card number
            boolean success = !request.getCardNumber().endsWith("0000");
            
            return PaymentResponse.builder()
                    .status(success ? "SUCCESS" : "FAILED")
                    .transactionId("stripe_" + System.currentTimeMillis())
                    .processedAt(LocalDateTime.now())
                    .failureReason(success ? null : "Card declined")
                    .build();
        } catch (Exception e) {
            return PaymentResponse.builder()
                    .status("FAILED")
                    .transactionId(null)
                    .processedAt(LocalDateTime.now())
                    .failureReason("Payment processing error")
                    .build();
        }
    }

    private PaymentResponse processPayPalPayment(PaymentRequest request) {
        // In real implementation, integrate with PayPal API
        try {
            Thread.sleep(1500);
            
            boolean success = !request.getCardNumber().endsWith("1111");
            
            return PaymentResponse.builder()
                    .status(success ? "SUCCESS" : "FAILED")
                    .transactionId("paypal_" + System.currentTimeMillis())
                    .processedAt(LocalDateTime.now())
                    .failureReason(success ? null : "PayPal payment failed")
                    .build();
        } catch (Exception e) {
            return PaymentResponse.builder()
                    .status("FAILED")
                    .transactionId(null)
                    .processedAt(LocalDateTime.now())
                    .failureReason("PayPal processing error")
                    .build();
        }
    }

    private PaymentResponse processDemoPayment(PaymentRequest request) {
        // Demo payment - always succeeds
        return PaymentResponse.builder()
                .status("SUCCESS")
                .transactionId("demo_" + System.currentTimeMillis())
                .processedAt(LocalDateTime.now())
                .failureReason(null)
                .build();
    }

    private PaymentResponse processStripeRefund(Payment payment, BigDecimal amount) {
        return PaymentResponse.builder()
                .status("REFUNDED")
                .transactionId("refund_stripe_" + System.currentTimeMillis())
                .processedAt(LocalDateTime.now())
                .failureReason(null)
                .build();
    }

    private PaymentResponse processPayPalRefund(Payment payment, BigDecimal amount) {
        return PaymentResponse.builder()
                .status("REFUNDED")
                .transactionId("refund_paypal_" + System.currentTimeMillis())
                .processedAt(LocalDateTime.now())
                .failureReason(null)
                .build();
    }

    private PaymentResponse processDemoRefund(Payment payment, BigDecimal amount) {
        return PaymentResponse.builder()
                .status("REFUNDED")
                .transactionId("refund_demo_" + System.currentTimeMillis())
                .processedAt(LocalDateTime.now())
                .failureReason(null)
                .build();
    }

    private PaymentResponse convertToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus().toString())
                .transactionId(payment.getTransactionId())
                .processedAt(payment.getProcessedAt())
                .failureReason(payment.getFailureReason())
                .notes(payment.getNotes())
                .build();
    }
}
