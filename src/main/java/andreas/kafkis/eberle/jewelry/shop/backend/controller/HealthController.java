package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.CacheService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health check and system status endpoints")
public class HealthController {

    private final MetricsService metricsService;
    private final CacheService cacheService;

    @Operation(
            summary = "Basic health check",
            description = "Returns basic system health status"
    )
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "Jewelry Shop Backend",
                "version", "1.0.0"
        ));
    }

    @Operation(
            summary = "Detailed health check",
            description = "Returns detailed system health including metrics and cache status"
    )
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "Jewelry Shop Backend",
                "version", "1.0.0",
                "metrics", Map.of(
                        "activeUsers", metricsService.getActiveUsers(),
                        "totalRevenue", metricsService.getTotalRevenue()
                ),
                "cache", Map.of(
                        "status", cacheService.getStats().getTotalKeys() > 0 ? "UP" : "DOWN",
                        "totalKeys", cacheService.getStats().getTotalKeys()
                ),
                "uptime", System.currentTimeMillis() - Long.parseLong(System.getProperty("java.vm.startTime", "0"))
        ));
    }

    @Operation(
            summary = "Metrics endpoint",
            description = "Returns current business metrics"
    )
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        return ResponseEntity.ok(Map.of(
                "activeUsers", metricsService.getActiveUsers(),
                "totalRevenue", metricsService.getTotalRevenue(),
                "timestamp", LocalDateTime.now()
        ));
    }
}
