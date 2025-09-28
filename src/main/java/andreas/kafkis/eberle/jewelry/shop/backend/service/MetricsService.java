package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    
    // Counters for tracking events
    private Counter userRegistrations;
    private Counter userLogins;
    private Counter ordersCreated;
    private Counter ordersCompleted;
    private Counter productsViewed;
    private Counter paymentsProcessed;
    
    // Timers for measuring performance
    private Timer orderProcessingTime;
    private Timer paymentProcessingTime;
    
    // Gauges for current state
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong totalRevenue = new AtomicLong(0);
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.userRegistrations = Counter.builder("jewelry.shop.user.registrations")
                .description("Total number of user registrations")
                .register(meterRegistry);
        
        this.userLogins = Counter.builder("jewelry.shop.user.logins")
                .description("Total number of user logins")
                .register(meterRegistry);
        
        this.ordersCreated = Counter.builder("jewelry.shop.orders.created")
                .description("Total number of orders created")
                .register(meterRegistry);
        
        this.ordersCompleted = Counter.builder("jewelry.shop.orders.completed")
                .description("Total number of orders completed")
                .register(meterRegistry);
        
        this.productsViewed = Counter.builder("jewelry.shop.products.viewed")
                .description("Total number of product views")
                .register(meterRegistry);
        
        this.paymentsProcessed = Counter.builder("jewelry.shop.payments.processed")
                .description("Total number of payments processed")
                .register(meterRegistry);
        
        // Initialize timers
        this.orderProcessingTime = Timer.builder("jewelry.shop.orders.processing.time")
                .description("Time taken to process orders")
                .register(meterRegistry);
        
        this.paymentProcessingTime = Timer.builder("jewelry.shop.payments.processing.time")
                .description("Time taken to process payments")
                .register(meterRegistry);
        
        // Register gauges
        Gauge.builder("jewelry.shop.users.active", activeUsers, AtomicLong::get)
                .description("Number of currently active users")
                .register(meterRegistry);
        
        Gauge.builder("jewelry.shop.revenue.total", totalRevenue, AtomicLong::get)
                .description("Total revenue generated")
                .register(meterRegistry);
    }
    
    // User metrics
    public void recordUserRegistration() {
        userRegistrations.increment();
    }
    
    public void recordUserLogin() {
        userLogins.increment();
        activeUsers.incrementAndGet();
    }
    
    public void recordUserLogout() {
        activeUsers.decrementAndGet();
    }
    
    // Order metrics
    public void recordOrderCreated() {
        ordersCreated.increment();
    }
    
    public void recordOrderCompleted() {
        ordersCompleted.increment();
    }
    
    public Timer.Sample startOrderProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordOrderProcessingTime(Timer.Sample sample) {
        sample.stop(orderProcessingTime);
    }
    
    // Product metrics
    public void recordProductView() {
        productsViewed.increment();
    }
    
    // Payment metrics
    public void recordPaymentProcessed(double amount) {
        paymentsProcessed.increment();
        totalRevenue.addAndGet((long) (amount * 100)); // Convert to cents for precision
    }
    
    public Timer.Sample startPaymentProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordPaymentProcessingTime(Timer.Sample sample) {
        sample.stop(paymentProcessingTime);
    }
    
    // Custom business metrics
    public void recordCustomMetric(String name, String description, double value) {
        Gauge.builder("jewelry.shop.custom." + name, () -> value)
                .description(description)
                .register(meterRegistry);
    }
    
    // Get current metrics
    public long getActiveUsers() {
        return activeUsers.get();
    }
    
    public double getTotalRevenue() {
        return totalRevenue.get() / 100.0; // Convert back from cents
    }
}

