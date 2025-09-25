package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import lombok.Data;

@Data
public class CreateImageRequest {
    private String storageKey;
    private String url;
    private boolean primary = false;
    private Integer sortOrder = 0;
    private String altText;
    private Integer width;
    private Integer height;
    private String mimeType;
}
