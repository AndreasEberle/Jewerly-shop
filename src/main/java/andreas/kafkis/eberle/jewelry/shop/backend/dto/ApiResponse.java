package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
}
