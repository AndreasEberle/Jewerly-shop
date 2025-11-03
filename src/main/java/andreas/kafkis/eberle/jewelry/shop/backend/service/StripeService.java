package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StripeService {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Value("${stripe.secret.key:}")
    private String stripeSecretKeyFromProperties;
    
    @Value("${stripe.publishable.key:}")
    private String stripePublishableKeyFromProperties;
    
    private String getStripeSecretKey() {
        // Priority 1: Check system config (database)
        String key = systemConfigService.getConfigValue("stripe.secret.key");
        if (key != null && !key.isEmpty() && !key.contains("CHANGE_THIS")) {
            return key;
        }
        
        // Priority 2: Check Spring properties (from application.properties or application-local.properties)
        if (stripeSecretKeyFromProperties != null && !stripeSecretKeyFromProperties.isEmpty() && 
            !stripeSecretKeyFromProperties.contains("CHANGE_THIS")) {
            return stripeSecretKeyFromProperties;
        }
        
        // Priority 3: Fallback to environment variable
        String envKey = System.getenv("STRIPE_SECRET_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }
        
        return null;
    }
    
    private String getStripePublishableKey() {
        // Priority 1: Check system config (database)
        String key = systemConfigService.getConfigValue("stripe.publishable.key");
        if (key != null && !key.isEmpty() && !key.contains("CHANGE_THIS")) {
            return key;
        }
        
        // Priority 2: Check Spring properties (from application.properties or application-local.properties)
        if (stripePublishableKeyFromProperties != null && !stripePublishableKeyFromProperties.isEmpty() && 
            !stripePublishableKeyFromProperties.contains("CHANGE_THIS")) {
            return stripePublishableKeyFromProperties;
        }
        
        // Priority 3: Fallback to environment variable
        String envKey = System.getenv("STRIPE_PUBLISHABLE_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }
        
        return null;
    }
    
    /**
     * Create a Stripe Payment Intent
     */
    public Map<String, Object> createPaymentIntent(Order order, String currency) {
        try {
            String secretKey = getStripeSecretKey();
            if (secretKey == null || secretKey.isEmpty()) {
                throw new RuntimeException("Stripe secret key not configured");
            }
            
            Stripe.apiKey = secretKey;
            
            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.toLowerCase())
                .setDescription("Order #" + order.getOrderNumber())
                .putMetadata("order_id", order.getId().toString())
                .putMetadata("order_number", order.getOrderNumber())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("amount", order.getTotalAmount());
            response.put("currency", currency);
            response.put("status", paymentIntent.getStatus());
            
            log.info("Created Stripe PaymentIntent {} for order {}", paymentIntent.getId(), order.getOrderNumber());
            
            return response;
            
        } catch (StripeException e) {
            log.error("Stripe error creating payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }
    
    /**
     * Confirm a payment intent (after customer completes payment)
     */
    public Map<String, Object> confirmPaymentIntent(String paymentIntentId) {
        try {
            Stripe.apiKey = getStripeSecretKey();
            
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("status", paymentIntent.getStatus());
            response.put("amount", BigDecimal.valueOf(paymentIntent.getAmount()).divide(BigDecimal.valueOf(100)));
            response.put("currency", paymentIntent.getCurrency().toUpperCase());
            
            if ("succeeded".equals(paymentIntent.getStatus())) {
                response.put("success", true);
                log.info("Payment confirmed: {}", paymentIntentId);
            } else {
                response.put("success", false);
                response.put("error", "Payment not completed. Status: " + paymentIntent.getStatus());
            }
            
            return response;
            
        } catch (StripeException e) {
            log.error("Stripe error confirming payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to confirm payment: " + e.getMessage());
        }
    }
    
    /**
     * Get payment intent status
     */
    public Map<String, Object> getPaymentIntentStatus(String paymentIntentId) {
        try {
            Stripe.apiKey = getStripeSecretKey();
            
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("status", paymentIntent.getStatus());
            response.put("amount", BigDecimal.valueOf(paymentIntent.getAmount()).divide(BigDecimal.valueOf(100)));
            response.put("currency", paymentIntent.getCurrency().toUpperCase());
            
            return response;
            
        } catch (StripeException e) {
            log.error("Stripe error getting payment status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get payment status: " + e.getMessage());
        }
    }
    
    /**
     * Create a refund
     */
    public Map<String, Object> createRefund(String paymentIntentId, BigDecimal amount) {
        try {
            Stripe.apiKey = getStripeSecretKey();
            
            // Retrieve payment intent with expanded charges
            PaymentIntentRetrieveParams retrieveParams = PaymentIntentRetrieveParams.builder()
                .addExpand("charges.data")
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId, retrieveParams, null);
            
            // Get the latest charge ID from the payment intent
            String chargeId = null;
            
            // Try to get charge ID from the payment intent's latest charge
            // In Stripe API, we need to list charges for this payment intent
            com.stripe.param.ChargeListParams chargeParams = com.stripe.param.ChargeListParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setLimit(1L)
                .build();
            
            com.stripe.model.ChargeCollection charges = Charge.list(chargeParams);
            
            if (charges.getData().isEmpty()) {
                throw new RuntimeException("No charges found for payment intent: " + paymentIntentId);
            }
            
            chargeId = charges.getData().get(0).getId();
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
            
            RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(chargeId)
                .setAmount(amountInCents)
                .build();
            
            Refund refund = Refund.create(params);
            
            Map<String, Object> response = new HashMap<>();
            response.put("refundId", refund.getId());
            response.put("status", refund.getStatus());
            response.put("amount", BigDecimal.valueOf(refund.getAmount()).divide(BigDecimal.valueOf(100)));
            response.put("currency", refund.getCurrency().toUpperCase());
            
            log.info("Created refund {} for payment intent {}", refund.getId(), paymentIntentId);
            
            return response;
            
        } catch (StripeException e) {
            log.error("Stripe error creating refund: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create refund: " + e.getMessage());
        }
    }
    
    /**
     * Get publishable key for frontend
     */
    public String getPublishableKey() {
        return getStripePublishableKey();
    }
    
    /**
     * Check if Stripe is configured
     */
    public boolean isConfigured() {
        String secretKey = getStripeSecretKey();
        return secretKey != null && !secretKey.isEmpty() && !secretKey.contains("CHANGE_THIS");
    }
    
    /**
     * Public method to get secret key (for testing environment detection)
     */
    public String getStripeSecretKeyPublic() {
        return getStripeSecretKey();
    }
    
    /**
     * Get Stripe environment (test or prod) from system config
     * Defaults to "test" if not configured
     */
    public String getStripeEnvironment() {
        String env = systemConfigService.getConfigValue("stripe.environment");
        if (env == null || env.isEmpty()) {
            return "test"; // Default to test for safety
        }
        return env.toLowerCase();
    }
    
    /**
     * Create a test PaymentIntent for testing connection
     * Uses currency-appropriate minimum amounts that meet Stripe requirements
     * CHF requires minimum 50 rappen (0.50 CHF), USD $0.50 works, EUR €0.50 works
     */
    public Map<String, Object> createTestPaymentIntent(String environment, String currency) {
        try {
            String secretKey = getStripeSecretKey();
            if (secretKey == null || secretKey.isEmpty()) {
                throw new RuntimeException("Stripe secret key not configured");
            }
            
            Stripe.apiKey = secretKey;
            
            // Use currency-appropriate minimum amounts
            // Stripe minimums: CHF = 50 rappen (0.50), USD = $0.50, EUR = €0.50, etc.
            String testCurrency = (currency != null && !currency.isEmpty()) ? currency.toLowerCase() : "chf";
            long amountInCents;
            
            // Map currency to minimum amount in smallest unit (cents/rappen)
            switch (testCurrency.toLowerCase()) {
                case "chf":
                    amountInCents = 50L; // 0.50 CHF = 50 rappen (minimum)
                    break;
                case "usd":
                    amountInCents = 50L; // $0.50 USD
                    break;
                case "eur":
                    amountInCents = 50L; // €0.50 EUR
                    break;
                case "gbp":
                    amountInCents = 30L; // £0.30 GBP (minimum)
                    break;
                case "jpy":
                    amountInCents = 50L; // ¥50 JPY (minimum)
                    break;
                default:
                    // Default to 1.00 in currency (safer minimum)
                    amountInCents = 100L; // 1.00 in any currency
                    testCurrency = "chf"; // Fallback currency
                    break;
            }
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(testCurrency)
                .setDescription("Test Payment - Stripe Connection Verification (" + environment + " mode)")
                .putMetadata("test_type", "connection_test")
                .putMetadata("environment", environment)
                .putMetadata("source", "admin_test_panel")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Note: PaymentIntent will be "incomplete" (requires_payment_method) - this is EXPECTED and CORRECT
            // The fact that the PaymentIntent was created and appears in Stripe Dashboard proves the connection works
            // To see it as "succeeded": In Stripe Dashboard -> Payments, click on this PaymentIntent and use test card "4242 4242 4242 4242" to complete it
            // This is just for connection testing, not for completing actual payments from the backend
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("status", paymentIntent.getStatus());
            response.put("amount", BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100)));
            response.put("currency", testCurrency.toUpperCase());
            response.put("clientSecret", paymentIntent.getClientSecret());
            
            // Add note about incomplete status
            if ("requires_payment_method".equals(paymentIntent.getStatus())) {
                response.put("note", "PaymentIntent is 'incomplete' (requires_payment_method) - this is EXPECTED and confirms your Stripe connection is working! To see it as 'succeeded': In Stripe Dashboard → Payments, click on this PaymentIntent and use test card '4242 4242 4242 4242' with any future expiry date and any CVC to complete it.");
            }
            
            log.info("Created test PaymentIntent {} for {} environment verification ({} {}). Status: {} - {} mode", 
                paymentIntent.getId(), environment, 
                BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100)), 
                testCurrency.toUpperCase(),
                paymentIntent.getStatus(),
                "test".equals(environment) ? "Incomplete status is expected for connection testing" : "LIVE");
            
            return response;
            
        } catch (StripeException e) {
            log.error("Stripe error creating test payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create test payment intent: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating test payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create test payment intent: " + e.getMessage());
        }
    }
    
    /**
     * Overload with default currency detection
     * Always uses CHF for test payments to avoid currency conversion issues
     */
    public Map<String, Object> createTestPaymentIntent(String environment) {
        // Always use CHF for test payments to match minimum amount requirements
        // CHF minimum is 50 rappen (0.50 CHF), which is what we use
        return createTestPaymentIntent(environment, "CHF");
    }
}

