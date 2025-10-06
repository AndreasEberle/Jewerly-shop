package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Admin operations for user management")
public class UserManagementController {
    
    private final UserRepository userRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with pagination and filtering")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "all") String role,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String auth,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        try {
            // Create sort object
            Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortBy);
            
            // Create pageable
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get users with pagination
            Page<User> userPage = userRepository.findAll(pageable);
            
            // Filter users based on criteria
            var filteredUsers = userPage.getContent().stream()
                    .filter(user -> {
                        // Search filter
                        boolean matchesSearch = search.isEmpty() ||
                                user.getEmail().toLowerCase().contains(search.toLowerCase()) ||
                                (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(search.toLowerCase())) ||
                                (user.getLastName() != null && user.getLastName().toLowerCase().contains(search.toLowerCase()));
                        
                        // Role filter
                        boolean matchesRole = role.equals("all") ||
                                (role.equals("admin") && user.getRoles().contains("ADMIN")) ||
                                (role.equals("user") && user.getRoles().contains("USER"));
                        
                        // Status filter
                        boolean matchesStatus = status.equals("all") ||
                                (status.equals("active") && user.isActive()) ||
                                (status.equals("inactive") && !user.isActive());
                        
                        // Auth filter
                        boolean matchesAuth = auth.equals("all") ||
                                (auth.equals("oauth") && user.isOauthOnly()) ||
                                (auth.equals("email") && !user.isOauthOnly());
                        
                        return matchesSearch && matchesRole && matchesStatus && matchesAuth;
                    })
                    .toList();
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("content", filteredUsers);
            response.put("totalElements", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            response.put("first", userPage.isFirst());
            response.put("last", userPage.isLast());
            response.put("numberOfElements", filteredUsers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting users: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PatchMapping("/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle user active status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable UUID userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            user.setActive(!user.isActive());
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User status updated successfully");
            response.put("active", user.isActive());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error toggling user status: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update user status: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            log.error("Error getting user: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
