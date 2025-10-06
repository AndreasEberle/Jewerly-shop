package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.AnalyticsDataDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.PageViewRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductAnalyticsRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.SystemMetricsRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserAnalyticsRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    
    private final UserAnalyticsRepository userAnalyticsRepository;
    private final ProductAnalyticsRepository productAnalyticsRepository;
    private final PageViewRepository pageViewRepository;
    private final SystemMetricsRepository systemMetricsRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    public AnalyticsDataDTO getAnalyticsData(String timeRange) {
        OffsetDateTime since = getTimeRangeStart(timeRange);
        
        // Get basic counts
        Long totalUsers = userRepository.count();
        Long totalProducts = productRepository.count();
        Long totalViews = pageViewRepository.countPageViewsSince(since);
        Long totalOrders = productAnalyticsRepository.countProductEventsByTypeSince("cart_add", since);
        
        // Get recent users (last 10 registrations)
        List<AnalyticsDataDTO.RecentUserDTO> recentUsers = userAnalyticsRepository.findRecentRegistrations()
                .stream()
                .limit(10)
                .map(ua -> AnalyticsDataDTO.RecentUserDTO.builder()
                        .id(ua.getUser().getId().toString())
                        .email(ua.getUser().getEmail())
                        .firstName(ua.getUser().getFirstName())
                        .lastName(ua.getUser().getLastName())
                        .createdAt(ua.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        // Get top products by views
        List<AnalyticsDataDTO.TopProductDTO> topProducts = productAnalyticsRepository.findTopProductsByViewsSince(since)
                .stream()
                .limit(10)
                .map(row -> {
                    String productId = row[0].toString();
                    String productName = row[1].toString();
                    Long views = (Long) row[2];
                    
                    // Get order count for this product
                    Long orders = productAnalyticsRepository.findTopProductsByOrdersSince(since)
                            .stream()
                            .filter(orderRow -> orderRow[0].toString().equals(productId))
                            .mapToLong(orderRow -> (Long) orderRow[2])
                            .sum();
                    
                    return AnalyticsDataDTO.TopProductDTO.builder()
                            .id(productId)
                            .name(productName)
                            .views(views)
                            .orders(orders)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Get country statistics
        List<AnalyticsDataDTO.CountryStatsDTO> countryStats = userAnalyticsRepository.findUserCountByCountrySince(since)
                .stream()
                .limit(10)
                .map(row -> {
                    String country = row[0].toString();
                    Long users = (Long) row[1];
                    
                    // Get views for this country
                    Long views = pageViewRepository.findPageViewsByCountrySince(since)
                            .stream()
                            .filter(viewRow -> viewRow[0].toString().equals(country))
                            .mapToLong(viewRow -> (Long) viewRow[1])
                            .sum();
                    
                    return AnalyticsDataDTO.CountryStatsDTO.builder()
                            .country(country)
                            .users(users)
                            .views(views)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Get daily statistics for the last 30 days
        List<AnalyticsDataDTO.DailyStatsDTO> dailyStats = pageViewRepository.findDailyPageViewsSince(since)
                .stream()
                .map(row -> {
                    String date = row[0].toString();
                    Long views = (Long) row[1];
                    
                    // For now, we'll use mock data for orders and users per day
                    // In a real implementation, you'd query the appropriate tables
                    Long orders = Math.round(views * 0.02); // Assume 2% conversion rate
                    Long users = Math.round(views * 0.1); // Assume 10% of views are unique users
                    
                    return AnalyticsDataDTO.DailyStatsDTO.builder()
                            .date(date)
                            .views(views)
                            .orders(orders)
                            .users(users)
                            .build();
                })
                .collect(Collectors.toList());
        
        return AnalyticsDataDTO.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalViews(totalViews)
                .totalOrders(totalOrders)
                .recentUsers(recentUsers)
                .topProducts(topProducts)
                .countryStats(countryStats)
                .dailyStats(dailyStats)
                .build();
    }
    
    private OffsetDateTime getTimeRangeStart(String timeRange) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (timeRange.toLowerCase()) {
            case "7d" -> now.minusDays(7);
            case "30d" -> now.minusDays(30);
            case "90d" -> now.minusDays(90);
            default -> now.minusDays(30);
        };
    }
    
    // Method to track user events
    public void trackUserEvent(String userId, String eventType, String eventData, String ipAddress, String userAgent, String countryCode) {
        try {
            // Implementation for tracking user events
            log.info("Tracking user event: {} for user: {}", eventType, userId);
        } catch (Exception e) {
            log.error("Failed to track user event: {}", e.getMessage(), e);
        }
    }
    
    // Method to track product events
    public void trackProductEvent(String productId, String userId, String eventType, String sessionId, String ipAddress, String userAgent, String countryCode) {
        try {
            // Implementation for tracking product events
            log.info("Tracking product event: {} for product: {}", eventType, productId);
        } catch (Exception e) {
            log.error("Failed to track product event: {}", e.getMessage(), e);
        }
    }
    
    // Method to track page views
    public void trackPageView(String userId, String pagePath, String pageTitle, String sessionId, String ipAddress, String userAgent, String countryCode, String referrer) {
        try {
            // Implementation for tracking page views
            log.info("Tracking page view: {} for user: {}", pagePath, userId);
        } catch (Exception e) {
            log.error("Failed to track page view: {}", e.getMessage(), e);
        }
    }
    
    // Method to track file uploads
    public void trackFileUpload(UUID productId, String filename, String contentType, long fileSize, String ipAddress, String userAgent) {
        try {
            // Implementation for tracking file uploads
            log.info("Tracking file upload: {} for product: {}", filename, productId);
        } catch (Exception e) {
            log.error("Failed to track file upload: {}", e.getMessage(), e);
        }
    }
    
    // Method to track product views
    public void trackProductView(UUID productId, String ipAddress, String userAgent) {
        try {
            // Implementation for tracking product views
            log.info("Tracking product view for product: {}", productId);
        } catch (Exception e) {
            log.error("Failed to track product view: {}", e.getMessage(), e);
        }
    }
}