package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDataDTO {
    
    private Long totalUsers;
    private Long totalProducts;
    private Long totalViews;
    private Long totalOrders;
    
    private List<RecentUserDTO> recentUsers;
    private List<TopProductDTO> topProducts;
    private List<CountryStatsDTO> countryStats;
    private List<DailyStatsDTO> dailyStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentUserDTO {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private OffsetDateTime createdAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductDTO {
        private String id;
        private String name;
        private Long views;
        private Long orders;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryStatsDTO {
        private String country;
        private Long users;
        private Long views;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyStatsDTO {
        private String date;
        private Long views;
        private Long orders;
        private Long users;
    }
}


