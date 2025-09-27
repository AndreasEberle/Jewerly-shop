package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    
    private String error;
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;
    private List<ValidationError> validationErrors;
    
    // Static factory methods for common error types
    public static ErrorResponse badRequest(String message, String path) {
        return ErrorResponse.builder()
                .error("Bad Request")
                .message(message)
                .status(400)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse unauthorized(String message, String path) {
        return ErrorResponse.builder()
                .error("Unauthorized")
                .message(message)
                .status(401)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse forbidden(String message, String path) {
        return ErrorResponse.builder()
                .error("Forbidden")
                .message(message)
                .status(403)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse notFound(String message, String path) {
        return ErrorResponse.builder()
                .error("Not Found")
                .message(message)
                .status(404)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse conflict(String message, String path) {
        return ErrorResponse.builder()
                .error("Conflict")
                .message(message)
                .status(409)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse internalServerError(String message, String path) {
        return ErrorResponse.builder()
                .error("Internal Server Error")
                .message(message)
                .status(500)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
