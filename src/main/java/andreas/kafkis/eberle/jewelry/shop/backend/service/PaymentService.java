package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Payment;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    
    public Page<PaymentDTO> getAllPayments(Pageable pageable, String status, String paymentMethod, String customerEmail) {
        Specification<Payment> spec = Specification.where(null);
        
        if (status != null && !status.isEmpty()) {
            try {
                Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), paymentStatus));
            } catch (IllegalArgumentException e) {
                // Invalid status value, ignore the filter
                log.warn("Invalid payment status filter: {}", status);
            }
        }
        
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("paymentMethod")), "%" + paymentMethod.toLowerCase() + "%"));
        }
        
        if (customerEmail != null && !customerEmail.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("order").get("customer").get("email")), "%" + customerEmail.toLowerCase() + "%"));
        }
        
        Page<Payment> payments = paymentRepository.findAll(spec, pageable);
        return payments.map(this::convertToDTO);
    }
    
    public PaymentDTO getPaymentById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
        return convertToDTO(payment);
    }
    
    public Map<String, Object> getPaymentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total payments
        long totalPayments = paymentRepository.count();
        stats.put("totalPayments", totalPayments);
        
        // Payments by status
        Map<String, Long> paymentsByStatus = new HashMap<>();
        for (Payment.PaymentStatus status : Payment.PaymentStatus.values()) {
            long count = paymentRepository.countByStatus(status.name());
            paymentsByStatus.put(status.name(), count);
        }
        stats.put("paymentsByStatus", paymentsByStatus);
        
        // Payments by method
        Map<String, Long> paymentsByMethod = paymentRepository.countByPaymentMethod();
        stats.put("paymentsByMethod", paymentsByMethod);
        
        // Recent payments (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentPayments = paymentRepository.countByCreatedAtAfter(thirtyDaysAgo);
        stats.put("recentPayments", recentPayments);
        
        // Total revenue
        BigDecimal totalRevenue = paymentRepository.sumAmountByStatus(Payment.PaymentStatus.COMPLETED.name());
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // Average payment amount
        BigDecimal avgPaymentAmount = paymentRepository.avgAmountByStatus(Payment.PaymentStatus.COMPLETED.name());
        stats.put("avgPaymentAmount", avgPaymentAmount != null ? avgPaymentAmount : BigDecimal.ZERO);
        
        return stats;
    }
    
    private PaymentDTO convertToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency("CHF") // Default currency since Payment entity doesn't have currency field
                .transactionId(payment.getTransactionId())
                .gatewayResponse(payment.getFailureReason()) // Using failureReason as gatewayResponse
                .processedAt(payment.getProcessedAt() != null ? payment.getProcessedAt().atOffset(java.time.ZoneOffset.UTC) : null)
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().atOffset(java.time.ZoneOffset.UTC) : null)
                .updatedAt(payment.getUpdatedAt() != null ? payment.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC) : null)
                .customerEmail(payment.getOrder().getCustomer().getEmail())
                .customerName(payment.getOrder().getCustomer().getFirstName() + " " + payment.getOrder().getCustomer().getLastName())
                .notes(payment.getNotes())
                .build();
    }
    
    /**
     * Process a payment request
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            // Find the order
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));
            
            // Create payment entity
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setNotes(request.getNotes());
            
            // For demo purposes, simulate payment processing
            // In a real implementation, this would integrate with payment gateways
            if ("demo".equals(request.getPaymentMethod())) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setProcessedAt(LocalDateTime.now());
            } else {
                // Simulate processing delay and success
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setProcessedAt(LocalDateTime.now());
            }
            
            // Save payment
            Payment savedPayment = paymentRepository.save(payment);
            
            // Update order status
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
            
            log.info("Payment processed successfully for order {}: {}", order.getOrderNumber(), savedPayment.getId());
            
            return PaymentResponse.builder()
                    .paymentId(savedPayment.getId())
                    .orderId(order.getId())
                    .paymentMethod(savedPayment.getPaymentMethod())
                    .amount(savedPayment.getAmount())
                    .status(savedPayment.getStatus().name())
                    .transactionId(savedPayment.getTransactionId())
                    .processedAt(savedPayment.getProcessedAt())
                    .notes(savedPayment.getNotes())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return PaymentResponse.builder()
                    .status("FAILED")
                    .failureReason(e.getMessage())
                    .build();
        }
    }
    
    /**
     * Get payment by ID
     */
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .processedAt(payment.getProcessedAt())
                .failureReason(payment.getFailureReason())
                .notes(payment.getNotes())
                .build();
    }
    
    /**
     * Refund a payment
     */
    public PaymentResponse refundPayment(UUID paymentId, BigDecimal amount) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
            
            if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
                throw new RuntimeException("Only successful payments can be refunded");
            }
            
            if (amount.compareTo(payment.getAmount()) > 0) {
                throw new RuntimeException("Refund amount cannot exceed payment amount");
            }
            
            // Create refund payment
            Payment refund = new Payment();
            refund.setOrder(payment.getOrder());
            refund.setAmount(amount.negate()); // Negative amount for refund
            refund.setPaymentMethod(payment.getPaymentMethod() + "_REFUND");
            refund.setStatus(Payment.PaymentStatus.COMPLETED);
            refund.setTransactionId(UUID.randomUUID().toString());
            refund.setProcessedAt(LocalDateTime.now());
            refund.setNotes("Refund for payment " + payment.getTransactionId());
            
            Payment savedRefund = paymentRepository.save(refund);
            
            log.info("Refund processed for payment {}: {}", paymentId, savedRefund.getId());
            
            return PaymentResponse.builder()
                    .paymentId(savedRefund.getId())
                    .orderId(savedRefund.getOrder().getId())
                    .paymentMethod(savedRefund.getPaymentMethod())
                    .amount(savedRefund.getAmount())
                    .status(savedRefund.getStatus().name())
                    .transactionId(savedRefund.getTransactionId())
                    .processedAt(savedRefund.getProcessedAt())
                    .notes(savedRefund.getNotes())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            return PaymentResponse.builder()
                    .status("FAILED")
                    .failureReason(e.getMessage())
                    .build();
        }
    }
}