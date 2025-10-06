package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.AnalyticsDataDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Analytics and reporting endpoints")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get analytics data", description = "Get comprehensive analytics data for the admin dashboard")
    public ResponseEntity<AnalyticsDataDTO> getAnalytics(
            @RequestParam(defaultValue = "30d") String range) {
        try {
            AnalyticsDataDTO analytics = analyticsService.getAnalyticsData(range);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting analytics data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/track/user-event")
    @Operation(summary = "Track user event", description = "Track a user analytics event")
    public ResponseEntity<Void> trackUserEvent(
            @RequestParam String userId,
            @RequestParam String eventType,
            @RequestParam(required = false) String eventData,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String userAgent,
            @RequestParam(required = false) String countryCode) {
        try {
            analyticsService.trackUserEvent(userId, eventType, eventData, ipAddress, userAgent, countryCode);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error tracking user event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/track/product-event")
    @Operation(summary = "Track product event", description = "Track a product analytics event")
    public ResponseEntity<Void> trackProductEvent(
            @RequestParam String productId,
            @RequestParam(required = false) String userId,
            @RequestParam String eventType,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String userAgent,
            @RequestParam(required = false) String countryCode) {
        try {
            analyticsService.trackProductEvent(productId, userId, eventType, sessionId, ipAddress, userAgent, countryCode);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error tracking product event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/track/page-view")
    @Operation(summary = "Track page view", description = "Track a page view event")
    public ResponseEntity<Void> trackPageView(
            @RequestParam(required = false) String userId,
            @RequestParam String pagePath,
            @RequestParam(required = false) String pageTitle,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String userAgent,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String referrer) {
        try {
            analyticsService.trackPageView(userId, pagePath, pageTitle, sessionId, ipAddress, userAgent, countryCode, referrer);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error tracking page view: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}