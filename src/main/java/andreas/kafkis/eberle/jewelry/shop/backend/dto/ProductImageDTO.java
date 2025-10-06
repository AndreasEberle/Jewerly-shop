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
public class ProductImageDTO {
    private UUID id;
    private String storageKey;
    private String url;
    private boolean isPrimary;
    private Integer sortOrder;
    private String altText;
    private Integer width;
    private Integer height;
    private String mimeType;
    private OffsetDateTime createdAt;
}
