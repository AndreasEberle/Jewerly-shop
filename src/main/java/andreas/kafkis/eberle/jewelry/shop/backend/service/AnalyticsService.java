package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.AnalyticsEvent;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.AnalyticsEventRepository;

@Service
public class AnalyticsService {
    
    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;
    
    /**
     * Track an analytics event
     */
    public void trackEvent(AnalyticsEvent.EventType eventType, UUID entityId, String entityType, 
                          String userIp, String userAgent, String referrer, String sessionId, 
                          Map<String, Object> additionalData) {
        
        AnalyticsEvent event = AnalyticsEvent.builder()
            .eventType(eventType)
            .entityId(entityId)
            .entityType(entityType)
            .userIp(userIp)
            .userAgent(userAgent)
            .referrer(referrer)
            .sessionId(sessionId)
            .additionalData(additionalData != null ? mapToJson(additionalData) : null)
            .createdAt(LocalDateTime.now())
            .build();
            
        analyticsEventRepository.save(event);
    }
    
    /**
     * Track a simple event without additional data
     */
    public void trackEvent(AnalyticsEvent.EventType eventType, UUID entityId, String entityType, 
                          String userIp, String userAgent) {
        trackEvent(eventType, entityId, entityType, userIp, userAgent, null, null, null);
    }
    
    /**
     * Track a product view
     */
    public void trackProductView(UUID productId, String userIp, String userAgent) {
        trackEvent(AnalyticsEvent.EventType.PRODUCT_VIEW, productId, "product", userIp, userAgent);
    }
    
    /**
     * Track a page view
     */
    public void trackPageView(String pagePath, String userIp, String userAgent) {
        trackEvent(AnalyticsEvent.EventType.PAGE_VIEW, null, "page", userIp, userAgent, 
                  null, null, Map.of("pagePath", pagePath));
    }
    
    /**
     * Track a search query
     */
    public void trackSearch(String query, String userIp, String userAgent) {
        trackEvent(AnalyticsEvent.EventType.PRODUCT_SEARCH, null, "search", userIp, userAgent, 
                  null, null, Map.of("query", query));
    }
    
    /**
     * Track a file upload
     */
    public void trackFileUpload(UUID productId, String fileName, String fileType, long fileSize, 
                               String userIp, String userAgent) {
        Map<String, Object> data = Map.of(
            "fileName", fileName,
            "fileType", fileType,
            "fileSize", fileSize
        );
        trackEvent(AnalyticsEvent.EventType.FILE_UPLOAD, productId, "product", userIp, userAgent, 
                  null, null, data);
    }
    
    /**
     * Track a category view
     */
    public void trackCategoryView(Integer categoryId, String userIp, String userAgent) {
        trackEvent(AnalyticsEvent.EventType.CATEGORY_VIEW, UUID.randomUUID(), "category", userIp, userAgent, 
                  null, null, Map.of("categoryId", categoryId));
    }
    
    /**
     * Track an API call
     */
    public void trackApiCall(String endpoint, String userIp, String userAgent) {
        trackEvent(AnalyticsEvent.EventType.API_CALL, null, "api", userIp, userAgent, 
                  null, null, Map.of("endpoint", endpoint));
    }
    
    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Total events today
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            stats.put("totalEventsToday", analyticsEventRepository.countByCreatedAtBetween(startOfDay, endOfDay));
            stats.put("productViewsToday", analyticsEventRepository.countByEventTypeAndCreatedAtBetween(
                AnalyticsEvent.EventType.PRODUCT_VIEW, startOfDay, endOfDay));
            stats.put("pageViewsToday", analyticsEventRepository.countByEventTypeAndCreatedAtBetween(
                AnalyticsEvent.EventType.PAGE_VIEW, startOfDay, endOfDay));
            stats.put("fileUploadsToday", analyticsEventRepository.countByEventTypeAndCreatedAtBetween(
                AnalyticsEvent.EventType.FILE_UPLOAD, startOfDay, endOfDay));
            
            // Most viewed products (last 7 days)
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            Pageable topTen = PageRequest.of(0, 10);
            List<Object[]> mostViewed = analyticsEventRepository.findMostViewedProducts(topTen);
            stats.put("mostViewedProducts", mostViewed);
            
            // Daily event counts (last 7 days)
            List<Object[]> dailyCounts = analyticsEventRepository.findDailyEventCounts(weekAgo);
            stats.put("dailyEventCounts", dailyCounts);
            
            // Hourly event counts (today)
            List<Object[]> hourlyCounts = analyticsEventRepository.findHourlyEventCounts(startOfDay, endOfDay);
            stats.put("hourlyEventCounts", hourlyCounts);
            
        } catch (Exception e) {
            // If analytics table doesn't exist yet, return zeros/empty data
            stats.put("totalEventsToday", 0L);
            stats.put("productViewsToday", 0L);
            stats.put("pageViewsToday", 0L);
            stats.put("fileUploadsToday", 0L);
            stats.put("mostViewedProducts", List.of());
            stats.put("dailyEventCounts", List.of());
            stats.put("hourlyEventCounts", List.of());
        }
        
        return stats;
    }
    
    /**
     * Get most viewed products
     */
    public List<Map<String, Object>> getMostViewedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = analyticsEventRepository.findMostViewedProducts(pageable);
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (Object[] result : results) {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", result[0]);
            product.put("viewCount", result[1]);
            products.add(product);
        }
        
        return products;
    }
    
    /**
     * Get recent search queries
     */
    public List<String> getRecentSearchQueries() {
        try {
            Pageable recent = PageRequest.of(0, 10);
            return analyticsEventRepository.findRecentSearchQueries(recent);
        } catch (Exception e) {
            // If analytics table doesn't exist yet, return empty list
            return List.of();
        }
    }
    
    /**
     * Get event counts by type
     */
    public Map<String, Long> getEventCountsByType() {
        Map<String, Long> counts = new HashMap<>();
        try {
            for (AnalyticsEvent.EventType type : AnalyticsEvent.EventType.values()) {
                Long count = analyticsEventRepository.countByEventType(type);
                counts.put(type.name(), count != null ? count : 0L);
            }
        } catch (Exception e) {
            // If analytics table doesn't exist yet, return zeros
            for (AnalyticsEvent.EventType type : AnalyticsEvent.EventType.values()) {
                counts.put(type.name(), 0L);
            }
        }
        return counts;
    }
    
    /**
     * Simple JSON conversion (for additional data)
     */
    private String mapToJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}
