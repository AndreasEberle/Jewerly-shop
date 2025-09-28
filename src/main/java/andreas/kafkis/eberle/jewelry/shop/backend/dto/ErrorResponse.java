package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private int status;
    private String error;
    private String message;
    private String path;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private String traceId;
    private List<ValidationError> validationErrors;
    private Map<String, Object> details;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
    
    // Static factory methods for common HTTP status codes
    public static ErrorResponse badRequest(String message, String path) {
        return ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse unauthorized(String message, String path) {
        return ErrorResponse.builder()
                .status(401)
                .error("Unauthorized")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse forbidden(String message, String path) {
        return ErrorResponse.builder()
                .status(403)
                .error("Forbidden")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse notFound(String message, String path) {
        return ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse conflict(String message, String path) {
        return ErrorResponse.builder()
                .status(409)
                .error("Conflict")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse internalServerError(String message, String path) {
        return ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}