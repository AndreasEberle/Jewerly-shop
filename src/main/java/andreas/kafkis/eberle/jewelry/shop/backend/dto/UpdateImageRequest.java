package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import lombok.Data;

@Data
public class UpdateImageRequest {
    private String altText;
    private Integer sortOrder;
    private boolean primary;
}
