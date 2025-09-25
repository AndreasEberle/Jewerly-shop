package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get dashboard statistics (JSON API)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get most viewed products
     */
    @GetMapping("/products/most-viewed")
    public ResponseEntity<List<Map<String, Object>>> getMostViewedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> products = analyticsService.getMostViewedProducts(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Get recent search queries
     */
    @GetMapping("/searches/recent")
    public ResponseEntity<List<String>> getRecentSearchQueries() {
        List<String> queries = analyticsService.getRecentSearchQueries();
        return ResponseEntity.ok(queries);
    }

    /**
     * Get event counts by type
     */
    @GetMapping("/events/counts")
    public ResponseEntity<Map<String, Long>> getEventCountsByType() {
        Map<String, Long> counts = analyticsService.getEventCountsByType();
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "*")
                .body(counts);
    }

    /**
     * Generate sample events for testing (visit this to create analytics data)
     */
    @GetMapping("/generate-test-events")
    public ResponseEntity<Map<String, Object>> generateTestEvents() {
        try {
            // Generate some sample analytics events
            UUID sampleProductId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            
            // Product views
            analyticsService.trackProductView(sampleProductId, "127.0.0.1", "Test Browser/1.0");
            analyticsService.trackProductView(sampleProductId, "127.0.0.1", "Test Browser/1.0");
            analyticsService.trackProductView(UUID.randomUUID(), "192.168.1.100", "Another Browser/2.0");
            
            // Page views  
            analyticsService.trackPageView("/", "127.0.0.1", "Test Browser/1.0");
            analyticsService.trackPageView("/products", "127.0.0.1", "Test Browser/1.0");
            analyticsService.trackPageView("/about", "192.168.1.100", "Another Browser/2.0");
            
            // Search queries
            analyticsService.trackSearch("gold rings", "127.0.0.1", "Test Browser/1.0");
            analyticsService.trackSearch("diamond necklace", "192.168.1.100", "Another Browser/2.0");
            
            // File uploads
            analyticsService.trackFileUpload(sampleProductId, "test-image.jpg", "image/jpeg", 2048576L, "127.0.0.1", "Test Browser/1.0");
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Generated 9 test analytics events",
                "events", Map.of(
                    "productViews", 3,
                    "pageViews", 3, 
                    "searches", 2,
                    "fileUploads", 1
                ),
                "tip", "Refresh the analytics dashboard to see the new data!"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Simple health check endpoint for testing
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        try {
            Map<String, String> response = Map.of(
                "status", "OK",
                "message", "Analytics API is working",
                "timestamp", java.time.LocalDateTime.now().toString(),
                "analyticsService", analyticsService != null ? "LOADED" : "NULL"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = Map.of(
                "status", "ERROR",
                "message", "Analytics API error: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test page endpoint - serves a simple HTML test page
     */
    @GetMapping("/test-page")
    public String testPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Analytics API Test</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .test-btn { padding: 10px 20px; margin: 10px; background: #007bff; color: white; border: none; cursor: pointer; border-radius: 5px; }
                    .result { margin: 20px 0; padding: 15px; border-radius: 5px; }
                    .success { background: #d4edda; border: 1px solid #c3e6cb; }
                    .error { background: #f8d7da; border: 1px solid #f5c6cb; }
                    pre { background: #f8f9fa; padding: 10px; border-radius: 3px; overflow-x: auto; }
                </style>
            </head>
            <body>
                <h1>Analytics API Test</h1>
                <button class="test-btn" onclick="testEndpoint('/api/analytics/health')">Test Health</button>
                <button class="test-btn" onclick="testEndpoint('/api/analytics/stats')">Test Stats</button>
                <button class="test-btn" onclick="testEndpoint('/api/analytics/events/counts')">Test Events</button>
                <div id="results"></div>

                <script>
                    async function testEndpoint(url) {
                        const results = document.getElementById('results');
                        const resultDiv = document.createElement('div');
                        resultDiv.innerHTML = '<h3>Testing ' + url + '...</h3>';
                        results.appendChild(resultDiv);

                        try {
                            const response = await fetch(url);
                            const data = await response.json();
                            resultDiv.className = 'result success';
                            resultDiv.innerHTML = '<h3>✅ Success: ' + url + '</h3><pre>' + JSON.stringify(data, null, 2) + '</pre>';
                        } catch (error) {
                            resultDiv.className = 'result error';
                            resultDiv.innerHTML = '<h3>❌ Error: ' + url + '</h3><p>' + error.message + '</p>';
                        }
                    }
                </script>
            </body>
            </html>
            """;
    }
}