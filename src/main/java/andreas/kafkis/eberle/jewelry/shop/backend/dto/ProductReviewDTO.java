package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewDTO {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID productId;
    private String productName;
    private Integer rating;
    private String title;
    private String content;
    private Boolean isVerifiedPurchase;
    private Boolean isApproved;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}



